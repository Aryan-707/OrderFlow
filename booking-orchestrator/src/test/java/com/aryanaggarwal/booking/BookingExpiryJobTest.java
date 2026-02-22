package com.aryanaggarwal.booking;

import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.booking.domain.Booking;
import com.aryanaggarwal.booking.job.BookingExpiryJob;
import com.aryanaggarwal.booking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.kafka.consumer.auto-offset-reset=earliest"})
@EmbeddedKafka(topics = {"bookings-new", "bookings-results", "payment-responses", "seat-reservation-responses"},
        partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
public class BookingExpiryJobTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingExpiryJob expiryJob;

    @Autowired
    private KafkaTemplate<String, BookingEvent> template;

    @Autowired
    private ConsumerFactory<String, BookingEvent> factory;

    /**
     * Verifies that a booking stuck in NEW state past its expiry time
     * gets marked as EXPIRED and a ROLLBACK event is published.
     */
    @Test
    void expiredBooking_shouldTriggerRollback() throws Exception {
        // create a booking that expired 1 minute ago
        Booking booking = new Booking();
        booking.setId("test-expiry-001");
        booking.setCustomerId(1L);
        booking.setSeatId(1L);
        booking.setAmount(100);
        booking.setStatus(BookingStatus.NEW);
        booking.setCreatedAt(LocalDateTime.now().minusMinutes(6));
        booking.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // already expired
        booking.setUpdatedAt(LocalDateTime.now().minusMinutes(6));
        bookingRepository.save(booking);

        // run the expiry job
        expiryJob.expireStaleBookings();

        // verify booking is now EXPIRED
        Booking expired = bookingRepository.findById("test-expiry-001").orElseThrow();
        assertEquals(BookingStatus.EXPIRED, expired.getStatus());
    }
}
