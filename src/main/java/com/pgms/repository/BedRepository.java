package com.pgms.repository;

import com.pgms.model.Bed;
import com.pgms.model.Enums.BedStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByStatus(BedStatus status, Sort sort);
    long countByStatus(BedStatus status);
}
