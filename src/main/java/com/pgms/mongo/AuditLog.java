package com.pgms.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("audit_logs") @Getter @Setter @NoArgsConstructor
public class AuditLog {
    @Id private String id;
    private String actor, action, entity, entityId, details;
    private Instant at;
}
