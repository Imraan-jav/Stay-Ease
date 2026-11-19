package com.pgms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "tenants") @Getter @Setter @NoArgsConstructor
public class Tenant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne @JoinColumn(name = "user_id") private User user;
    private String phone;
    private String idProofType;
    private String idProofNumber;
    private String guardianPhone;
    private LocalDate joinedOn;
}
