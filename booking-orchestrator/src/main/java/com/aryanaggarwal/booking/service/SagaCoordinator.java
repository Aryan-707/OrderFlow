package com.aryanaggarwal.booking.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;

/**
 * Handles the core SAGA decision logic. When both payment and seat-inventory
 * services respond, this class determines the final booking outcome.
 */
@Service
public class SagaCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(SagaCoordinator.class);

    private final Counter bookingsInitiated;
    private final Counter bookingsConfirmed;
    private final Counter bookingsRolledBack;
    private final Counter bookingsExpired;

    public SagaCoordinator(MeterRegistry registry) {
        this.bookingsInitiated = Counter.builder("saga_bookings_initiated_total")
                .description("Total booking requests received")
                .register(registry);

        this.bookingsConfirmed = Counter.builder("saga_bookings_confirmed_total")
                .description("Bookings where both services accepted")
                .register(registry);

        this.bookingsRolledBack = Counter.builder("saga_bookings_rollback_total")
                .description("Bookings that triggered compensating rollback")
                .register(registry);

        this.bookingsExpired = Counter.builder("saga_bookings_expired_total")
                .description("Bookings expired by the scheduled expiry job")
                .register(registry);
    }

    /**
     * Called by Kafka Streams join — receives both payment and seat responses.
     * Determines the final booking status based on individual service outcomes.
     */
    public BookingEvent resolveOutcome(BookingEvent paymentResponse, BookingEvent seatResponse) {
        BookingEvent result = new BookingEvent(
                paymentResponse.getId(),
                paymentResponse.getCustomerId(),
                paymentResponse.getSeatId(),
                paymentResponse.getStatus());

        result.setSeatCount(paymentResponse.getSeatCount());
        result.setAmount(paymentResponse.getAmount());

        if (paymentResponse.getStatus() == BookingStatus.ACCEPTED &&
                seatResponse.getStatus() == BookingStatus.ACCEPTED) {
            result.setStatus(BookingStatus.CONFIRMED);
            bookingsConfirmed.increment();
            LOG.info("[SAGA] Booking {} -> payment: ACCEPTED | seat: ACCEPTED | CONFIRMED",
                    result.getId());

        } else if (paymentResponse.getStatus() == BookingStatus.REJECTED &&
                seatResponse.getStatus() == BookingStatus.REJECTED) {
            result.setStatus(BookingStatus.REJECTED);
            LOG.info("[SAGA] Booking {} -> payment: REJECTED | seat: REJECTED | REJECTED",
                    result.getId());

        } else {
            // one accepted, one rejected — need compensating transaction
            String rejectSource = paymentResponse.getStatus() == BookingStatus.REJECTED
                    ? "PAYMENT" : "SEAT";
            result.setStatus(BookingStatus.ROLLBACK);
            result.setSource(rejectSource);
            bookingsRolledBack.increment();
            LOG.info("[SAGA] Booking {} -> ROLLBACK triggered by {}", result.getId(), rejectSource);
        }

        return result;
    }

    public void recordInitiated() {
        bookingsInitiated.increment();
    }

    public void recordExpired() {
        bookingsExpired.increment();
    }
}
