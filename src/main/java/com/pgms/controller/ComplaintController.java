package com.pgms.controller;

import com.pgms.dto.Dtos;
import com.pgms.model.Complaint;
import com.pgms.repository.ComplaintRepository;
import com.pgms.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/complaints") @RequiredArgsConstructor
public class ComplaintController {
    private final ComplaintService svc;
    private final ComplaintRepository repo;

    @PostMapping @PreAuthorize("hasRole('TENANT')")
    public Complaint create(@Valid @RequestBody Dtos.ComplaintRequest r, Authentication a) { return svc.create(a.getName(), r); }

    @GetMapping("/mine") @PreAuthorize("hasRole('TENANT')")
    public List<Complaint> mine(Authentication a) { return svc.mine(a.getName()); }

    @GetMapping @PreAuthorize("hasRole('ADMIN')")
    public List<Complaint> all() { return repo.findAllByOrderByCreatedAtDesc(); }

    @PatchMapping("/{id}/status") @PreAuthorize("hasRole('ADMIN')")
    public Complaint status(@PathVariable Long id, @Valid @RequestBody Dtos.StatusRequest r) { return svc.updateStatus(id, r.status()); }
}
