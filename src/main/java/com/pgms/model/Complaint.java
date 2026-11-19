package com.pgms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "complaints") @Getter @Setter @NoArgsConstructor
public class Complaint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "tenant_id") private Tenant tenant;
    private String title;
    private String description;
    private String category;
    @Enumerated(EnumType.STRING) private Enums.ComplaintStatus status = Enums.ComplaintStatus.OPEN;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
