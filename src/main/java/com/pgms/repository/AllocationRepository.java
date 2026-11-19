package com.pgms.repository;

import com.pgms.model.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByActiveTrue();
    Optional<Allocation> findByTenantIdAndActiveTrue(Long tenantId);
    long countByActiveTrue();
}
