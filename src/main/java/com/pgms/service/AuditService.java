package com.pgms.service;

import com.pgms.mongo.AuditLog;
import com.pgms.mongo.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

/** Writes audit trail documents to MongoDB. Never breaks a business operation if Mongo is down. */
@Service @RequiredArgsConstructor @Slf4j
public class AuditService {
    private final AuditLogRepository repo;

    public void log(String action, String entity, Object entityId, String details) {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            AuditLog l = new AuditLog();
            l.setActor(a != null ? a.getName() : "system");
            l.setAction(action); l.setEntity(entity);
            l.setEntityId(String.valueOf(entityId)); l.setDetails(details); l.setAt(Instant.now());
            repo.save(l);
        } catch (Exception e) {
            log.warn("Audit entry skipped (MongoDB unavailable): {}", e.getMessage());
        }
    }

    public List<AuditLog> recent() { return repo.findTop50ByOrderByAtDesc(); }
}
