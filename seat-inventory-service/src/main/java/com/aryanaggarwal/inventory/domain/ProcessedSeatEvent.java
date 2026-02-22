package com.aryanaggarwal.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_seat_events")
public class ProcessedSeatEvent {
    @Id
    private String orderId;
    private LocalDateTime processedAt;

    public ProcessedSeatEvent() {}
    public ProcessedSeatEvent(String orderId) {
        this.orderId = orderId;
        this.processedAt = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
