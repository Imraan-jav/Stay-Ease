package com.pgms.service;

import com.pgms.dto.Dtos;
import com.pgms.exception.ApiException;
import com.pgms.model.*;
import com.pgms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service @RequiredArgsConstructor
public class TenantService {
    private final UserRepository users;
    private final TenantRepository tenants;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    @Transactional
    public Tenant create(Dtos.TenantRequest r) {
        if (users.existsByEmail(r.email())) throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        User u = new User();
        u.setName(r.name()); u.setEmail(r.email()); u.setRole(Enums.Role.TENANT);
        u.setPassword(encoder.encode(r.password() != null ? r.password() : "Welcome@123"));
        users.save(u);
        Tenant t = new Tenant();
        t.setUser(u); t.setPhone(r.phone()); t.setIdProofType(r.idProofType());
        t.setIdProofNumber(r.idProofNumber()); t.setGuardianPhone(r.guardianPhone()); t.setJoinedOn(LocalDate.now());
        tenants.save(t);
        audit.log("CREATE_TENANT", "Tenant", t.getId(), r.name());
        return t;
    }
}
