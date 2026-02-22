package com.aryanaggarwal.booking.domain;

import com.aryanaggarwal.base.domain.BookingStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a booking in the orchestrator's database.
 * Separate from BookingEvent (the Kafka message) — this tracks saga state
 * and enables the expiry job to find stuck bookings.
 */
@Entity
@Table(name = "booking")
public class Booking {

    @Id
    private String id; // UUID from BookingEvent

    private Long customerId;
    private Long seatId;
    private int amount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version; // optimistic lock — prevents concurrent modification

    public Booking() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
