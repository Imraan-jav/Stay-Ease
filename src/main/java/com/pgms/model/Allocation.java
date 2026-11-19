package com.pgms.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "allocations") @Getter @Setter @NoArgsConstructor
public class Allocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "tenant_id") private Tenant tenant;
    @ManyToOne @JoinColumn(name = "bed_id") private Bed bed;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyRent;
    private BigDecimal deposit;
    private boolean active = true;
}
