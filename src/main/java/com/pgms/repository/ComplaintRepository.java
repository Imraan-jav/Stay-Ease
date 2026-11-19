package com.pgms.repository;

import com.pgms.model.Complaint;
import com.pgms.model.Enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<Complaint> findAllByOrderByCreatedAtDesc();
    long countByStatusNot(ComplaintStatus status);
}
