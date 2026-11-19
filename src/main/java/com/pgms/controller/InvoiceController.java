package com.pgms.controller;

import com.pgms.dto.Dtos;
import com.pgms.exception.ApiException;
import com.pgms.model.*;
import com.pgms.repository.*;
import com.pgms.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/invoices") @RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService svc;
    private final InvoiceRepository invoices;
    private final TenantRepository tenants;

    @GetMapping @PreAuthorize("hasRole('ADMIN')")
    public List<Invoice> all() { svc.refreshOverdue(); return invoices.findAllByOrderByDueDateDesc(); }

    @GetMapping("/mine") @PreAuthorize("hasRole('TENANT')")
    public List<Invoice> mine(Authentication auth) {
        svc.refreshOverdue();
        Tenant t = tenants.findByUserEmail(auth.getName()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tenant profile not found"));
        return invoices.findByAllocationTenantIdOrderByDueDateDesc(t.getId());
    }

    @PostMapping("/generate") @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> generate(@RequestParam String month) {
        try { return Map.of("month", month, "created", svc.generateForMonth(YearMonth.parse(month))); }
        catch (DateTimeParseException e) { throw new ApiException(HttpStatus.BAD_REQUEST, "month must be YYYY-MM"); }
    }

    @PostMapping("/{id}/pay")
    public Payment pay(@PathVariable Long id, @Valid @RequestBody Dtos.PaymentRequest r, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return svc.pay(id, r, auth.getName(), admin);
    }
}
