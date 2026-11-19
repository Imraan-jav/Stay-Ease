package com.pgms.controller;

import com.pgms.dto.Dtos;
import com.pgms.model.*;
import com.pgms.repository.*;
import com.pgms.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/tenants") @PreAuthorize("hasRole('ADMIN')") @RequiredArgsConstructor
public class TenantController {
    private final TenantService svc;
    private final TenantRepository tenants;

    @PostMapping public Tenant create(@Valid @RequestBody Dtos.TenantRequest r) { return svc.create(r); }
    @GetMapping public List<Tenant> list() { return tenants.findAll(Sort.by(Sort.Direction.DESC, "id")); }
}
