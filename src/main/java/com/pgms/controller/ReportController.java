package com.pgms.controller;

import com.pgms.model.Enums;
import com.pgms.model.Invoice;
import com.pgms.mongo.AuditLog;
import com.pgms.repository.*;
import com.pgms.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController @RequestMapping("/api") @PreAuthorize("hasRole('ADMIN')") @RequiredArgsConstructor
public class ReportController {
    private final BedRepository beds;
    private final AllocationRepository allocs;
    private final InvoiceRepository invoices;
    private final PaymentRepository payments;
    private final ComplaintRepository complaints;
    private final AuditService audit;

    @GetMapping("/reports/dashboard")
    public Map<String, Object> dashboard() {
        long total = beds.count(), occ = beds.countByStatus(Enums.BedStatus.OCCUPIED);
        BigDecimal outstanding = invoices.findByStatusIn(List.of(Enums.InvoiceStatus.PENDING, Enums.InvoiceStatus.OVERDUE))
                .stream().map((Invoice i) -> i.getAmount().add(i.getLateFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Map<String, Object>> monthly = new ArrayList<>();
        for (Object[] row : payments.monthlyRevenue()) monthly.add(Map.of("month", row[0], "total", row[1]));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalBeds", total); m.put("occupiedBeds", occ); m.put("availableBeds", total - occ);
        m.put("occupancyRate", total == 0 ? 0 : Math.round(occ * 1000.0 / total) / 10.0);
        m.put("activeTenants", allocs.countByActiveTrue());
        m.put("outstanding", outstanding); m.put("collected", payments.totalCollected());
        m.put("openComplaints", complaints.countByStatusNot(Enums.ComplaintStatus.RESOLVED));
        m.put("monthlyRevenue", monthly);
        return m;
    }

    @GetMapping("/audit") public List<AuditLog> audit() { return audit.recent(); }
}
