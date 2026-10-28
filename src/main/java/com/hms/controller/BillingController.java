package com.hms.controller;

import com.hms.dto.InvoicePayDto;
import com.hms.mock.MockBillingService;
import com.hms.mock.MockExaminationService;
import com.hms.model.Examination;
import com.hms.model.Invoice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/billing")
public class BillingController {

    private final MockBillingService billingService;
    private final MockExaminationService examinationService;

    public BillingController(MockBillingService billingService,
                             MockExaminationService examinationService) {
        this.billingService = billingService;
        this.examinationService = examinationService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String status,
                        Model model) {
        List<Invoice> invoices = billingService.searchInvoices(search, status);
        long totalPaid = billingService.calculateTotalRevenue();
        long totalDue = billingService.calculatePendingRevenue();

        model.addAttribute("activeNav", "billing");
        model.addAttribute("invoices", invoices);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalDue", totalDue);
        model.addAttribute("totalCount", invoices.size());
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        model.addAttribute("payForm", new InvoicePayDto());

        return "billing/index";
    }

    @PostMapping("/pay")
    public String payInvoice(@ModelAttribute InvoicePayDto form, RedirectAttributes ra) {
        boolean ok = billingService.payInvoice(form.getIdHoadon(), form.getPhuongThucTt(), form.getThoiGianThu());
        if (ok) {
            ra.addFlashAttribute("successMessage", "Đã thu thành công hóa đơn " + form.getIdHoadon() + " (" + form.getPhuongThucTt() + ").");
        }
        return "redirect:/billing";
    }

    @GetMapping("/print/{id}")
    public String printInvoice(@PathVariable String id, Model model, RedirectAttributes ra) {
        Invoice invoice = billingService.getInvoiceById(id);
        if (invoice == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy hóa đơn " + id);
            return "redirect:/billing";
        }

        Examination exam = examinationService.getExaminationById(invoice.getIdPhieukham());
        model.addAttribute("invoice", invoice);
        model.addAttribute("exam", exam);
        return "billing/print";
    }
}

