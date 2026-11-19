package com.pgms.repository;

import com.pgms.model.Invoice;
import com.pgms.model.Enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByAllocationIdAndMonthYear(Long allocationId, String monthYear);
    boolean existsByAllocationTenantIdAndStatusNot(Long tenantId, InvoiceStatus status);
    List<Invoice> findByStatusIn(List<InvoiceStatus> statuses);
    List<Invoice> findByAllocationTenantIdOrderByDueDateDesc(Long tenantId);
    List<Invoice> findAllByOrderByDueDateDesc();
}
