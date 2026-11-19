package com.pgms.repository;

import com.pgms.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("select coalesce(sum(p.amount), 0) from Payment p")
    BigDecimal totalCollected();

    @Query(value = "select to_char(paid_at,'YYYY-MM') as m, sum(amount) as total from payments group by 1 order by 1 desc limit 6", nativeQuery = true)
    List<Object[]> monthlyRevenue();
}
