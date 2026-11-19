package com.pgms.service;

import com.pgms.dto.Dtos;
import com.pgms.exception.ApiException;
import com.pgms.model.*;
import com.pgms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor
public class ComplaintService {
    private final ComplaintRepository complaints;
    private final TenantRepository tenants;
    private final AuditService audit;

    private Tenant tenant(String email) {
        return tenants.findByUserEmail(email).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tenant profile not found"));
    }

    public Complaint create(String email, Dtos.ComplaintRequest r) {
        Complaint c = new Complaint();
        c.setTenant(tenant(email)); c.setTitle(r.title()); c.setDescription(r.description()); c.setCategory(r.category());
        c.setStatus(Enums.ComplaintStatus.OPEN); c.setCreatedAt(LocalDateTime.now()); c.setUpdatedAt(LocalDateTime.now());
        Complaint s = complaints.save(c);
        audit.log("CREATE_COMPLAINT", "Complaint", s.getId(), s.getTitle());
        return s;
    }

    public List<Complaint> mine(String email) { return complaints.findByTenantIdOrderByCreatedAtDesc(tenant(email).getId()); }

    public Complaint updateStatus(Long id, Enums.ComplaintStatus status) {
        Complaint c = complaints.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found"));
        c.setStatus(status); c.setUpdatedAt(LocalDateTime.now());
        complaints.save(c);
        audit.log("COMPLAINT_STATUS", "Complaint", id, "-> " + status);
        return c;
    }
}
