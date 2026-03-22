package com.aryanaggarwal.payment;

import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.payment.domain.Customer;
import com.aryanaggarwal.payment.repository.CustomerRepository;
import com.aryanaggarwal.payment.repository.ProcessedPaymentEventRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that sending the same booking event twice
 * only charges the customer once. Kafka guarantees at-least-once
 * delivery, so consumers must be idempotent.
 */
@SpringBootTest(properties = {"spring.kafka.consumer.auto-offset-reset=earliest"})
@EmbeddedKafka(topics = {"bookings-new", "payment-responses"},
        partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
public class PaymentIdempotencyTest {

    @Autowired
    private KafkaTemplate<String, BookingEvent> template;
    @Autowired
    private ConsumerFactory<String, BookingEvent> factory;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProcessedPaymentEventRepository processedRepo;

    @Test
    void duplicatePaymentEvent_shouldBeProcessedOnce() throws Exception {
        // get customer 1's initial balance
        Customer before = customerRepository.findById(1L).orElseThrow();
        int initialAvailable = before.getAmountAvailable();

        // create a booking with a fixed ID to simulate duplicate
        BookingEvent event = new BookingEvent(1L, 1L, 1, 50);
        String bookingId = event.getId();

        // send the same event twice — simulates Kafka rebalance replay
        template.send("bookings-new", bookingId, event).get(1000, TimeUnit.MILLISECONDS);
        Thread.sleep(2000); // wait for first processing

        template.send("bookings-new", bookingId, event).get(1000, TimeUnit.MILLISECONDS);
        Thread.sleep(2000); // wait for second (should be skipped)

        // customer should only have been charged once
        Customer after = customerRepository.findById(1L).orElseThrow();
        assertEquals(initialAvailable - 50, after.getAmountAvailable(),
                "Customer should only be charged once despite duplicate event");

        // idempotency table should have exactly one record for this booking
        assertTrue(processedRepo.existsById(bookingId));
    }
}
