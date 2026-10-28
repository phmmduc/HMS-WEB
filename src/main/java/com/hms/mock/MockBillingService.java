package com.hms.mock;

import com.hms.model.Examination;
import com.hms.model.Invoice;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MockBillingService {

    private final MockDataStore store;

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
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        store.linkAll();
        return getAllInvoices().stream()
                .filter(inv -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status) && !status.equalsIgnoreCase(inv.getTrangThai())) {
                        return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (inv.getIdHoadon() + " " + inv.getIdPhieukham() + " " + (inv.getPatientName() != null ? inv.getPatientName() : "") + " " + (inv.getIdBn() != null ? inv.getIdBn() : "")).toLowerCase();
                    return target.contains(kw);
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
}

