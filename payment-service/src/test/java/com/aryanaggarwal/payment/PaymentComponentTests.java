package com.aryanaggarwal.payment;

import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.payment.domain.Customer;
import com.aryanaggarwal.payment.repository.CustomerRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.kafka.consumer.auto-offset-reset=earliest"})
@EmbeddedKafka(topics = {"bookings-new", "payment-responses"},
        partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentComponentTests {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentComponentTests.class);
    static Customer customer;

    @Autowired
    private KafkaTemplate<String, BookingEvent> template;
    @Autowired
    private ConsumerFactory<String, BookingEvent> factory;
    @Autowired
    CustomerRepository repository;

    @Test
    @Order(1)
    void eventAccept() throws Exception {
        BookingEvent event = new BookingEvent(1L, 1L, 2, 100);
        SendResult<String, BookingEvent> r = template.send("bookings-new", event.getId(), event)
                .get(1000, TimeUnit.MILLISECONDS);
        LOG.info("Sent: {}", r.getProducerRecord().value());

        template.setConsumerFactory(factory);
        ConsumerRecord<String, BookingEvent> rec = template.receive("payment-responses", 0, 0, Duration.ofSeconds(5));
        assertNotNull(rec);
        assertNotNull(rec.value());
        assertEquals(BookingStatus.ACCEPTED, rec.value().getStatus());

        customer = repository.findById(1L).orElseThrow();
    }

    @Test
    @Order(2)
    void eventReject() throws Exception {
        BookingEvent event = new BookingEvent(2L, 2L, 2, 10000);
        template.send("bookings-new", event.getId(), event).get(1000, TimeUnit.MILLISECONDS);

        template.setConsumerFactory(factory);
        ConsumerRecord<String, BookingEvent> rec = template.receive("payment-responses", 0, 1, Duration.ofSeconds(5));
        assertNotNull(rec);
        assertNotNull(rec.value());
        assertEquals(BookingStatus.REJECTED, rec.value().getStatus());
    }

    @Test
    @Order(3)
    void eventConfirm() throws Exception {
        BookingEvent event = new BookingEvent(customer.getId().toString(), 1L, 1L, BookingStatus.CONFIRMED);
        event.setAmount(100);
        template.send("bookings-results", event.getId(), event).get(1000, TimeUnit.MILLISECONDS);

        Thread.sleep(3000);
        Customer c = repository.findById(1L).orElseThrow();
        assertEquals(customer.getAmountAvailable(), c.getAmountAvailable());
        assertEquals(0, c.getAmountReserved());
    }
}
