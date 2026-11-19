package com.pgms.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "invoices") @Getter @Setter @NoArgsConstructor
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "allocation_id") private Allocation allocation;
    private String monthYear;
    private BigDecimal amount;
    private BigDecimal lateFee = BigDecimal.ZERO;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING) private Enums.InvoiceStatus status = Enums.InvoiceStatus.PENDING;
}
