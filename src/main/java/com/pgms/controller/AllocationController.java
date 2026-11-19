package com.pgms.controller;

import com.pgms.dto.Dtos;
import com.pgms.model.Allocation;
import com.pgms.repository.AllocationRepository;
import com.pgms.service.AllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/allocations") @PreAuthorize("hasRole('ADMIN')") @RequiredArgsConstructor
public class AllocationController {
    private final AllocationService svc;
    private final AllocationRepository allocs;

    @GetMapping public List<Allocation> list() { return allocs.findAll(Sort.by(Sort.Direction.DESC, "id")); }
    @PostMapping public Allocation allocate(@Valid @RequestBody Dtos.AllocationRequest r) { return svc.allocate(r); }
    @PostMapping("/{id}/checkout") public Allocation checkout(@PathVariable Long id) { return svc.checkout(id); }
    @PostMapping("/{id}/transfer")
    public Allocation transfer(@PathVariable Long id, @Valid @RequestBody Dtos.TransferRequest r) { return svc.transfer(id, r.newBedId()); }
}
