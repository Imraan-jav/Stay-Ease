package com.pgms.controller;

import com.pgms.exception.ApiException;
import com.pgms.model.User;
import com.pgms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController @RequestMapping("/api/me") @RequiredArgsConstructor
public class MeController {
    private final UserRepository users;
    private final TenantRepository tenants;
    private final AllocationRepository allocations;

    @GetMapping
    public Map<String, Object> me(Authentication auth) {
        User u = users.findByEmail(auth.getName()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", u.getName()); m.put("email", u.getEmail()); m.put("role", u.getRole());
        tenants.findByUserEmail(auth.getName()).ifPresent(t -> {
            m.put("tenant", t);
            allocations.findByTenantIdAndActiveTrue(t.getId()).ifPresent(a -> m.put("allocation", a));
        });
        return m;
    }
}
