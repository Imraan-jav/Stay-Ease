package com.pgms.controller;

import com.pgms.dto.Dtos;
import com.pgms.mongo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController @RequestMapping("/api/notices") @RequiredArgsConstructor
public class NoticeController {
    private final NoticeRepository repo;

    @GetMapping public List<Notice> list() { return repo.findTop20ByOrderByPostedAtDesc(); }

    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public Notice post(@Valid @RequestBody Dtos.NoticeRequest r, Authentication a) {
        Notice n = new Notice();
        n.setTitle(r.title()); n.setMessage(r.message()); n.setPostedBy(a.getName()); n.setPostedAt(Instant.now());
        return repo.save(n);
    }
}
