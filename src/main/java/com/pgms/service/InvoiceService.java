package com.pgms.service;

import com.pgms.dto.Dtos;
import com.pgms.exception.ApiException;
import com.pgms.model.*;
import com.pgms.model.Enums.InvoiceStatus;
import com.pgms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service @RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoices;
    private final PaymentRepository payments;
    private final AllocationRepository allocations;
    private final AuditService audit;
    @Value("${app.late-fee-per-day}") private BigDecimal lateFeePerDay;

    /** Creates the invoice for a month if missing. First month is prorated by days stayed. Returns null if it already existed. */
    @Transactional
    public Invoice createIfMissing(Allocation a, YearMonth ym) {
        String key = ym.toString();
        if (invoices.existsByAllocationIdAndMonthYear(a.getId(), key)) return null;
        BigDecimal amount = a.getMonthlyRent();
        LocalDate due = ym.atDay(5);
        if (YearMonth.from(a.getStartDate()).equals(ym)) {
            int days = ym.lengthOfMonth() - a.getStartDate().getDayOfMonth() + 1;
            amount = amount.multiply(BigDecimal.valueOf(days))
                    .divide(BigDecimal.valueOf(ym.lengthOfMonth()), 2, RoundingMode.HALF_UP);
            LocalDate grace = a.getStartDate().plusDays(5);
            if (grace.isAfter(due)) due = grace;
        }
        Invoice i = new Invoice();
        i.setAllocation(a); i.setMonthYear(key); i.setAmount(amount); i.setDueDate(due);
        i.setLateFee(BigDecimal.ZERO); i.setStatus(InvoiceStatus.PENDING);
        return invoices.save(i);
    }

    @Transactional
    public int generateForMonth(YearMonth ym) {
        int created = 0;
        for (Allocation a : allocations.findByActiveTrue()) {
            if (a.getStartDate().isAfter(ym.atEndOfMonth())) continue;
            if (createIfMissing(a, ym) != null) created++;
        }
        audit.log("GENERATE_INVOICES", "Invoice", ym.toString(), created + " invoices created");
        return created;
    }

    /** Flags unpaid invoices past their due date and applies a per-day late fee (capped at 25% of the amount). */
    @Transactional
    public int refreshOverdue() {
        LocalDate today = LocalDate.now();
        int n = 0;
        for (Invoice i : invoices.findByStatusIn(List.of(InvoiceStatus.PENDING, InvoiceStatus.OVERDUE))) {
            if (!i.getDueDate().isBefore(today)) continue;
            long days = ChronoUnit.DAYS.between(i.getDueDate(), today);
            BigDecimal fee = lateFeePerDay.multiply(BigDecimal.valueOf(days)).min(i.getAmount().multiply(new BigDecimal("0.25")));
            i.setStatus(InvoiceStatus.OVERDUE);
            i.setLateFee(fee.setScale(2, RoundingMode.HALF_UP));
            n++;
        }
        return n;
    }

    @Transactional
    public Payment pay(Long invoiceId, Dtos.PaymentRequest r, String email, boolean admin) {
        Invoice i = invoices.findById(invoiceId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice not found"));
        if (!admin && !i.getAllocation().getTenant().getUser().getEmail().equals(email))
            throw new ApiException(HttpStatus.FORBIDDEN, "This invoice belongs to another tenant");
        if (i.getStatus() == InvoiceStatus.PAID) throw new ApiException(HttpStatus.CONFLICT, "Invoice already paid");
        Payment p = new Payment();
        p.setInvoice(i); p.setAmount(i.getAmount().add(i.getLateFee()));
        p.setMethod(r.method()); p.setReference(r.reference()); p.setPaidAt(LocalDateTime.now());
        payments.save(p);
        i.setStatus(InvoiceStatus.PAID);
        audit.log("PAYMENT", "Invoice", i.getId(), "Paid " + p.getAmount() + " via " + r.method());
        return p;
    }
}
