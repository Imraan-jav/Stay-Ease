package com.pgms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "beds") @Getter @Setter @NoArgsConstructor
public class Bed {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "room_id") private Room room;
    private String bedLabel;
    @Enumerated(EnumType.STRING) private Enums.BedStatus status = Enums.BedStatus.AVAILABLE;
}
