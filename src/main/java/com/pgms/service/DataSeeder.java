package com.pgms.service;

import com.pgms.dto.Dtos;
import com.pgms.model.*;
import com.pgms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component @RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final RoomRepository rooms;
    private final RoomService roomService;
    private final PasswordEncoder encoder;
    @Value("${app.admin.email}") private String adminEmail;
    @Value("${app.admin.password}") private String adminPassword;
    @Value("${app.seed-demo}") private boolean seedDemo;

    @Override
    public void run(String... args) {
        if (!users.existsByEmail(adminEmail)) {
            User u = new User();
            u.setName("Owner"); u.setEmail(adminEmail); u.setRole(Enums.Role.ADMIN); u.setPassword(encoder.encode(adminPassword));
            users.save(u);
        }
        if (seedDemo && rooms.count() == 0) {
            roomService.create(new Dtos.RoomRequest("101", 1, "DOUBLE", new BigDecimal("6000"), 2));
            roomService.create(new Dtos.RoomRequest("102", 1, "TRIPLE", new BigDecimal("5000"), 3));
            roomService.create(new Dtos.RoomRequest("201", 2, "SINGLE", new BigDecimal("9000"), 1));
        }
    }
}
