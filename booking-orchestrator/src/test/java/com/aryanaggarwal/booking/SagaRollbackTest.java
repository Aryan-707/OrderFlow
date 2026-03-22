package com.aryanaggarwal.booking;

import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.booking.service.SagaCoordinator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the core SAGA join logic — when one service accepts
 * but the other rejects, the coordinator must trigger a ROLLBACK.
 */
public class SagaRollbackTest {

    private SagaCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new SagaCoordinator(new SimpleMeterRegistry());
    }

    @Test
    void whenSeatRejects_paymentShouldRollback() {
        // payment accepted the booking
        BookingEvent paymentResponse = new BookingEvent(1L, 1L, 2, 200);
        paymentResponse.setStatus(BookingStatus.ACCEPTED);
        paymentResponse.setSource("payment");

        // seat rejected — not enough availability
        BookingEvent seatResponse = new BookingEvent(1L, 1L, 2, 200);
        seatResponse.setId(paymentResponse.getId()); // same booking ID
        seatResponse.setStatus(BookingStatus.REJECTED);
        seatResponse.setSource("seat-inventory");

        // coordinator resolves the conflict
        BookingEvent result = coordinator.resolveOutcome(paymentResponse, seatResponse);

        assertEquals(BookingStatus.ROLLBACK, result.getStatus());
        assertEquals("SEAT", result.getSource()); // seat was the rejecting service
    }

    @Test
    void whenPaymentRejects_seatShouldRollback() {
        BookingEvent paymentResponse = new BookingEvent(1L, 1L, 2, 200);
        paymentResponse.setStatus(BookingStatus.REJECTED);
        paymentResponse.setSource("payment");

        BookingEvent seatResponse = new BookingEvent(1L, 1L, 2, 200);
        seatResponse.setId(paymentResponse.getId());
        seatResponse.setStatus(BookingStatus.ACCEPTED);
        seatResponse.setSource("seat-inventory");

        BookingEvent result = coordinator.resolveOutcome(paymentResponse, seatResponse);

        assertEquals(BookingStatus.ROLLBACK, result.getStatus());
        assertEquals("PAYMENT", result.getSource());
    }

    @Test
    void whenBothAccept_shouldConfirm() {
        BookingEvent paymentResponse = new BookingEvent(1L, 1L, 2, 200);
        paymentResponse.setStatus(BookingStatus.ACCEPTED);

        BookingEvent seatResponse = new BookingEvent(1L, 1L, 2, 200);
        seatResponse.setId(paymentResponse.getId());
        seatResponse.setStatus(BookingStatus.ACCEPTED);

        BookingEvent result = coordinator.resolveOutcome(paymentResponse, seatResponse);

        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void whenBothReject_shouldReject() {
        BookingEvent paymentResponse = new BookingEvent(1L, 1L, 2, 200);
        paymentResponse.setStatus(BookingStatus.REJECTED);

        BookingEvent seatResponse = new BookingEvent(1L, 1L, 2, 200);
        seatResponse.setId(paymentResponse.getId());
        seatResponse.setStatus(BookingStatus.REJECTED);

        BookingEvent result = coordinator.resolveOutcome(paymentResponse, seatResponse);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }
}
