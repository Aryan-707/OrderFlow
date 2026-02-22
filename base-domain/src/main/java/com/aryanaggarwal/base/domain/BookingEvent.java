package com.aryanaggarwal.base.domain;

import java.util.UUID;

/**
 * Core event object that flows through all Kafka topics.
 * This is the shared contract between booking-orchestrator,
 * payment-service, and seat-inventory-service.
 */
public class BookingEvent {

    private String id;
    private Long customerId;
    private Long seatId;
    private int seatCount;
    private int amount;
    private BookingStatus status;
    private String source; // which service produced this response

    public BookingEvent() {
        // Jackson needs a no-arg constructor for deserialization
    }

    public BookingEvent(Long customerId, Long seatId, int seatCount, int amount) {
        this.id = UUID.randomUUID().toString(); // safe across restarts and multiple instances
        this.customerId = customerId;
        this.seatId = seatId;
        this.seatCount = seatCount;
        this.amount = amount;
        this.status = BookingStatus.NEW;
    }

    public BookingEvent(String id, Long customerId, Long seatId, BookingStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.seatId = seatId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "BookingEvent{" +
                "id='" + id + '\'' +
                ", customerId=" + customerId +
                ", seatId=" + seatId +
                ", seatCount=" + seatCount +
                ", amount=" + amount +
                ", status=" + status +
                ", source='" + source + '\'' +
                '}';
    }
}
