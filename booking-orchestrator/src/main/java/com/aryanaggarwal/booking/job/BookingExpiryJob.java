package com.aryanaggarwal.booking.job;

import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.booking.domain.Booking;
import com.aryanaggarwal.booking.repository.BookingRepository;
import com.aryanaggarwal.booking.service.SagaCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically sweeps for bookings stuck in intermediate states.
 * If a booking's expiry time has passed and neither payment nor seat
 * service has responded, this job marks it EXPIRED and publishes
 * a ROLLBACK event so both services can release any partial reservations.
 */
@Component
public class BookingExpiryJob {

    private static final Logger LOG = LoggerFactory.getLogger(BookingExpiryJob.class);

    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;
    private final SagaCoordinator sagaCoordinator;

    public BookingExpiryJob(BookingRepository bookingRepository,
                            KafkaTemplate<String, BookingEvent> kafkaTemplate,
                            SagaCoordinator sagaCoordinator) {
        this.bookingRepository = bookingRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.sagaCoordinator = sagaCoordinator;
    }

    @Scheduled(fixedDelay = 30000) // runs every 30 seconds
    public void expireStaleBookings() {
        List<Booking> expired = bookingRepository.findExpiredBookings(
                List.of(BookingStatus.NEW), // find bookings still in NEW state
                LocalDateTime.now()
        );

        for (Booking booking : expired) {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            // build a rollback event so both services can compensate
            BookingEvent rollback = new BookingEvent(
                    booking.getId(), booking.getCustomerId(),
                    booking.getSeatId(), BookingStatus.ROLLBACK);
            rollback.setAmount(booking.getAmount());
            rollback.setSource("EXPIRY"); // marks this rollback as timeout-triggered
            kafkaTemplate.send("bookings-results", booking.getId(), rollback);

            sagaCoordinator.recordExpired();
            LOG.warn("[SAGA] Booking {} EXPIRED -> ROLLBACK published", booking.getId());
        }
    }
}
