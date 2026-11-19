package com.pgms.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "payments") @Getter @Setter @NoArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "invoice_id") private Invoice invoice;
    private BigDecimal amount;
    private String method;
    private String reference;
    private LocalDateTime paidAt;
}
