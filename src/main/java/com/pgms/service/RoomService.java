package com.pgms.service;

import com.pgms.dto.Dtos;
import com.pgms.model.*;
import com.pgms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class RoomService {
    private final RoomRepository rooms;
    private final BedRepository beds;
    private final AuditService audit;

    @Transactional
    public Room create(Dtos.RoomRequest r) {
        Room room = new Room();
        room.setRoomNumber(r.roomNumber()); room.setFloor(r.floor());
        room.setRoomType(r.roomType()); room.setRentPerBed(r.rentPerBed());
        rooms.save(room);
        for (int i = 0; i < r.beds(); i++) {
            Bed b = new Bed();
            b.setRoom(room); b.setBedLabel(String.valueOf((char) ('A' + i))); b.setStatus(Enums.BedStatus.AVAILABLE);
            beds.save(b);
        }
        audit.log("CREATE_ROOM", "Room", room.getId(), "Room " + room.getRoomNumber() + " with " + r.beds() + " beds");
        return room;
    }
}
