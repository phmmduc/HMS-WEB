package com.hms.mock;

import com.hms.dto.RevenueReportDto;
import com.hms.model.Examination;
import com.hms.model.Invoice;
import com.hms.service.BillingDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MockBillingService {

    private final MockDataStore store;

    @Autowired(required = false)
    private BillingDbService billingDbService;

    public MockBillingService(MockDataStore store) {
        this.store = store;
    }

    public List<Invoice> getAllInvoices() {
        store.linkAll();
        return new ArrayList<>(store.invoices.values()).stream()
                .sorted((a, b) -> b.getIdHoadon().compareTo(a.getIdHoadon()))
                .collect(Collectors.toList());
    }

    public Invoice getInvoiceById(String id) {
        if (id == null) return null;
        store.linkAll();
        return store.invoices.get(id);
    }

    public List<Invoice> searchInvoices(String keyword, String status) {
        return filterInvoices(keyword, status, null, null, null, null, null);
    }

    public List<Invoice> filterInvoices(String keyword, String status, String viewType, Integer year, Integer month, String date, String selectedKey) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        store.linkAll();

        return getAllInvoices().stream()
                .filter(inv -> {
                    // 1. Trạng thái thanh toán
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status) && !status.equalsIgnoreCase(inv.getTrangThai())) {
                        return false;
                    }

                    // 2. Lọc theo từ khóa
                    if (!kw.isEmpty()) {
                        String target = (inv.getIdHoadon() + " " + inv.getIdPhieukham() + " " + (inv.getPatientName() != null ? inv.getPatientName() : "") + " " + (inv.getIdBn() != null ? inv.getIdBn() : "")).toLowerCase();
                        if (!target.contains(kw)) return false;
                    }

                    // 3. Lọc theo thời gian drill-down hoặc view
                    String effDate = inv.getEffectiveDate();
                    if (selectedKey != null && !selectedKey.trim().isEmpty()) {
                        return effDate.startsWith(selectedKey.trim());
                    }

                    if ("day".equalsIgnoreCase(viewType) && date != null && !date.trim().isEmpty()) {
                        return effDate.equals(date.trim());
                    }

                    if ("year".equalsIgnoreCase(viewType) && year != null) {
                        return effDate.startsWith(String.format("%04d", year));
                    }

                    if ("month".equalsIgnoreCase(viewType) && year != null && month != null) {
                        return effDate.startsWith(String.format("%04d-%02d", year, month));
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    public synchronized boolean payInvoice(String invoiceId, String method, String paymentDate) {
        Invoice inv = store.invoices.get(invoiceId);
        if (inv == null) return false;

        inv.setTrangThai("Đã thanh toán");
        inv.setPhuongThucTt(method != null && !method.isEmpty() ? method : "Tiền mặt");
        inv.setThoiGianThu(paymentDate != null && !paymentDate.isEmpty() ? paymentDate : LocalDate.now().toString());

        // Check if all invoices for this exam are paid, update exam to "Hoàn tất" if appropriate
        String examId = inv.getIdPhieukham();
        boolean hasPending = store.invoices.values().stream()
                .filter(i -> examId.equals(i.getIdPhieukham()))
                .anyMatch(i -> !"Đã thanh toán".equalsIgnoreCase(i.getTrangThai()));

        if (!hasPending) {
            Examination exam = store.examinations.get(examId);
            if (exam != null) {
                exam.setTrangThai("Hoàn tất");
            }
        }

        store.linkAll();

        if (billingDbService != null) {
            try {
                billingDbService.payInvoice(invoiceId, method, null);
            } catch (Exception ignored) {}
        }

        return true;
    }

    public long calculateTotalRevenue() {
        return store.invoices.values().stream()
                .filter(Invoice::isPaid)
                .mapToLong(Invoice::getSoTien)
                .sum();
    }

    public long calculatePendingRevenue() {
        return store.invoices.values().stream()
                .filter(i -> !i.isPaid())
                .mapToLong(Invoice::getSoTien)
                .sum();
    }

    /**
     * Tạo dữ liệu tổng hợp doanh thu và dữ liệu biểu đồ tương tác
     */
    public RevenueReportDto getRevenueReport(String viewType, Integer year, Integer month, String date, String selectedKey) {
        LocalDate today = LocalDate.now();
        String vt = (viewType != null && !viewType.trim().isEmpty()) ? viewType.trim().toLowerCase() : "month";
        int y = (year != null && year > 1900) ? year : today.getYear();
        int m = (month != null && month >= 1 && month <= 12) ? month : today.getMonthValue();
        String d = (date != null && !date.trim().isEmpty()) ? date.trim() : today.toString();
        String selKey = (selectedKey != null) ? selectedKey.trim() : "";

        RevenueReportDto report = new RevenueReportDto();
        report.setViewType(vt);
        report.setYear(y);
        report.setMonth(m);
        report.setDate(d);
        report.setSelectedKey(selKey);

        List<Invoice> all = getAllInvoices();

        if ("year".equals(vt)) {
            // Xem 12 tháng của năm y
            for (int monthIdx = 1; monthIdx <= 12; monthIdx++) {
                String monthKey = String.format("%04d-%02d", y, monthIdx);
                List<Invoice> inMonth = all.stream()
                        .filter(inv -> inv.getEffectiveDate().startsWith(monthKey))
                        .collect(Collectors.toList());

                long paid = inMonth.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum();
                long due = inMonth.stream().filter(inv -> !inv.isPaid()).mapToLong(Invoice::getSoTien).sum();

                report.getChartLabels().add("Tháng " + monthIdx);
                report.getChartPaidData().add(paid);
                report.getChartDueData().add(due);
                report.getChartKeys().add(monthKey);
            }

            // Nếu người dùng click vào một tháng cụ thể
            if (!selKey.isEmpty()) {
                List<Invoice> selInvoices = all.stream().filter(inv -> inv.getEffectiveDate().startsWith(selKey)).collect(Collectors.toList());
                report.setTotalPaid(selInvoices.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum());
                report.setTotalDue(selInvoices.stream().filter(inv -> !inv.isPaid()).mapToLong(Invoice::getSoTien).sum());
                report.setTotalCount(selInvoices.size());
            } else {
                String yearKey = String.format("%04d", y);
                List<Invoice> yearInvoices = all.stream().filter(inv -> inv.getEffectiveDate().startsWith(yearKey)).collect(Collectors.toList());
                report.setTotalPaid(yearInvoices.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum());
                report.setTotalDue(yearInvoices.stream().filter(inv -> !inv.isPaid()).mapToLong(Invoice::getSoTien).sum());
                report.setTotalCount(yearInvoices.size());
            }

        } else if ("day".equals(vt)) {
            // Xem theo ngày cụ thể (và các ngày xung quanh trong tuần)
            LocalDate targetDate;
            try {
                targetDate = LocalDate.parse(d);
            } catch (Exception e) {
                targetDate = today;
            }

            for (int offset = -3; offset <= 3; offset++) {
                LocalDate cur = targetDate.plusDays(offset);
                String dayKey = cur.toString();
                List<Invoice> inDay = all.stream().filter(inv -> dayKey.equals(inv.getEffectiveDate())).collect(Collectors.toList());

                long paid = inDay.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum();
                long due = inDay.stream().filter(inv -> !inv.isPaid()).mapToLong(Invoice::getSoTien).sum();

                report.getChartLabels().add(cur.getDayOfMonth() + "/" + cur.getMonthValue());
                report.getChartPaidData().add(paid);
                report.getChartDueData().add(due);
                report.getChartKeys().add(dayKey);
            }

            String activeKey = !selKey.isEmpty() ? selKey : targetDate.toString();
            List<Invoice> dayInvoices = all.stream().filter(inv -> activeKey.equals(inv.getEffectiveDate())).collect(Collectors.toList());
            report.setTotalPaid(dayInvoices.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum());
            report.setTotalDue(dayInvoices.stream().filter(inv -> !inv.isPaid()).mapToLong(Invoice::getSoTien).sum());
            report.setTotalCount(dayInvoices.size());

        } else {
            // Mặc định: Xem theo các ngày trong tháng (ngày 1 đến 28/30/31)
            YearMonth ym = YearMonth.of(y, m);
            int daysInMonth = ym.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                String dayKey = String.format("%04d-%02d-%02d", y, m, day);
                List<Invoice> inDay = all.stream().filter(inv -> dayKey.equals(inv.getEffectiveDate())).collect(Collectors.toList());

                long paid = inDay.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum();
                long due = inDay.stream().filter(inv -> !inv.isPaid()).mapToLong(Invoice::getSoTien).sum();

                report.getChartLabels().add(String.valueOf(day));
                report.getChartPaidData().add(paid);
                report.getChartDueData().add(due);
                report.getChartKeys().add(dayKey);
            }

            // Nếu người dùng click vào một ngày cụ thể
            if (!selKey.isEmpty()) {
                List<Invoice> selInvoices = all.stream().filter(inv -> selKey.equals(inv.getEffectiveDate())).collect(Collectors.toList());
                report.setTotalPaid(selInvoices.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum());
                report.setTotalDue(selInvoices.stream().filter(inv -> !inv.isPaid()).mapToLong(Invoice::getSoTien).sum());
                report.setTotalCount(selInvoices.size());
            } else {
                String monthKey = String.format("%04d-%02d", y, m);
                List<Invoice> monthInvoices = all.stream().filter(inv -> inv.getEffectiveDate().startsWith(monthKey)).collect(Collectors.toList());
                report.setTotalPaid(monthInvoices.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum());
                report.setTotalDue(monthInvoices.stream().filter(inv -> !inv.isPaid()).mapToLong(Invoice::getSoTien).sum());
                report.setTotalCount(monthInvoices.size());
            }
        }

        return report;
    }
}

