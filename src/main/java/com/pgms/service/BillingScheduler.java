package com.pgms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.YearMonth;

@Component @RequiredArgsConstructor @Slf4j
public class BillingScheduler {
    private final InvoiceService invoices;

    @Scheduled(cron = "0 0 1 1 * *")
    public void monthlyInvoices() { log.info("Monthly invoices created: {}", invoices.generateForMonth(YearMonth.now())); }

    @Scheduled(cron = "0 30 1 * * *")
    public void dailyOverdue() { log.info("Invoices marked overdue: {}", invoices.refreshOverdue()); }

    /** Free hosting tiers sleep, so cron can be missed. Catch up on every startup (generation is idempotent). */
    @EventListener(ApplicationReadyEvent.class)
    public void catchUp() { monthlyInvoices(); dailyOverdue(); }
}
