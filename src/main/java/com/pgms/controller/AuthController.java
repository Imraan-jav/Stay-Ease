package com.pgms.controller;

import com.pgms.dto.Dtos;
import com.pgms.exception.ApiException;
import com.pgms.model.User;
import com.pgms.repository.UserRepository;
import com.pgms.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody Dtos.LoginRequest r) {
        User u = users.findByEmail(r.email()).filter(User::isActive)
                .filter(x -> encoder.matches(r.password(), x.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return Map.of("token", jwt.generate(u.getEmail(), u.getRole().name()), "role", u.getRole(), "name", u.getName());
    }
}
