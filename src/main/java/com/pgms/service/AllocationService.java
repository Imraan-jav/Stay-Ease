package com.pgms.service;

import com.pgms.dto.Dtos;
import com.pgms.exception.ApiException;
import com.pgms.model.*;
import com.pgms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service @RequiredArgsConstructor
public class AllocationService {
    private final AllocationRepository allocs;
    private final TenantRepository tenants;
    private final BedRepository beds;
    private final InvoiceRepository invoices;
    private final InvoiceService invoiceService;
    private final AuditService audit;

    @Transactional
    public Allocation allocate(Dtos.AllocationRequest r) {
        Tenant t = tenant(r.tenantId());
        if (allocs.findByTenantIdAndActiveTrue(t.getId()).isPresent())
            throw new ApiException(HttpStatus.CONFLICT, "Tenant already has an active bed");
        Allocation a = open(t, r.bedId(), r.monthlyRent(), r.deposit(), r.startDate());
        audit.log("ALLOCATE", "Allocation", a.getId(), t.getUser().getName() + " -> bed " + a.getBed().getId());
        return a;
    }

    @Transactional
    public Allocation checkout(Long id) {
        Allocation a = active(id);
        if (invoices.existsByAllocationTenantIdAndStatusNot(a.getTenant().getId(), Enums.InvoiceStatus.PAID))
            throw new ApiException(HttpStatus.CONFLICT, "Clear pending dues before checkout");
        close(a);
        audit.log("CHECKOUT", "Allocation", a.getId(), a.getTenant().getUser().getName());
        return a;
    }

    @Transactional
    public Allocation transfer(Long id, Long newBedId) {
        Allocation old = active(id);
        close(old);   // flushed first so the partial unique index (one active allocation per tenant) is satisfied
        Allocation n = open(old.getTenant(), newBedId, old.getMonthlyRent(), old.getDeposit(), LocalDate.now());
        audit.log("TRANSFER", "Allocation", n.getId(), "bed " + old.getBed().getId() + " -> bed " + n.getBed().getId());
        return n;
    }

    private Allocation open(Tenant t, Long bedId, BigDecimal rent, BigDecimal deposit, LocalDate start) {
        Bed bed = beds.findById(bedId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bed not found"));
        if (bed.getStatus() != Enums.BedStatus.AVAILABLE) throw new ApiException(HttpStatus.CONFLICT, "Bed is not available");
        bed.setStatus(Enums.BedStatus.OCCUPIED);
        beds.save(bed);
        Allocation a = new Allocation();
        a.setTenant(t); a.setBed(bed); a.setActive(true);
        a.setStartDate(start != null ? start : LocalDate.now());
        a.setMonthlyRent(rent != null ? rent : bed.getRoom().getRentPerBed());
        a.setDeposit(deposit != null ? deposit : BigDecimal.ZERO);
        a = allocs.saveAndFlush(a);   // concurrent double booking fails here -> 409 via DB unique index
        invoiceService.createIfMissing(a, YearMonth.from(a.getStartDate()));
        return a;
    }

    private void close(Allocation a) {
        a.setActive(false); a.setEndDate(LocalDate.now());
        a.getBed().setStatus(Enums.BedStatus.AVAILABLE);
        beds.save(a.getBed());
        allocs.saveAndFlush(a);
    }

    private Tenant tenant(Long id) { return tenants.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tenant not found")); }

    private Allocation active(Long id) {
        Allocation a = allocs.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Allocation not found"));
        if (!a.isActive()) throw new ApiException(HttpStatus.CONFLICT, "Allocation is already closed");
        return a;
    }
}
