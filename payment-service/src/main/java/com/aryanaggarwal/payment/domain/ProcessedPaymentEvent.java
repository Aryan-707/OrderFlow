package com.aryanaggarwal.payment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Tracks processed booking IDs to prevent duplicate charge on Kafka replay */
@Entity
@Table(name = "processed_payment_events")
public class ProcessedPaymentEvent {
    @Id
    private String orderId;
    private LocalDateTime processedAt;

    public ProcessedPaymentEvent() {}

    public ProcessedPaymentEvent(String orderId) {
        this.orderId = orderId;
        this.processedAt = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
