package com.pgms.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "rooms") @Getter @Setter @NoArgsConstructor
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String roomNumber;
    private Integer floor;
    private String roomType;
    private BigDecimal rentPerBed;
}
