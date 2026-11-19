package com.pgms.controller;

import com.pgms.dto.Dtos;
import com.pgms.model.*;
import com.pgms.repository.*;
import com.pgms.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api") @PreAuthorize("hasRole('ADMIN')") @RequiredArgsConstructor
public class RoomController {
    private final RoomService svc;
    private final RoomRepository rooms;
    private final BedRepository beds;

    @PostMapping("/rooms") public Room create(@Valid @RequestBody Dtos.RoomRequest r) { return svc.create(r); }
    @GetMapping("/rooms") public List<Room> rooms() { return rooms.findAll(); }

    @GetMapping("/beds")
    public List<Bed> beds(@RequestParam(required = false) Enums.BedStatus status) {
        Sort sort = Sort.by("room.roomNumber", "bedLabel");
        return status == null ? beds.findAll(sort) : beds.findByStatus(status, sort);
    }
}
