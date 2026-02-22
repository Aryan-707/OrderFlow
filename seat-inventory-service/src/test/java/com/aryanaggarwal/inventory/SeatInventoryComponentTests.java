package com.aryanaggarwal.inventory;

import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.inventory.domain.Seat;
import com.aryanaggarwal.inventory.repository.SeatRepository;
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
@EmbeddedKafka(topics = {"bookings-new", "seat-reservation-responses"},
        partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeatInventoryComponentTests {

    private static final Logger LOG = LoggerFactory.getLogger(SeatInventoryComponentTests.class);
    static Seat seat;

    @Autowired
    private KafkaTemplate<String, BookingEvent> template;
    @Autowired
    private ConsumerFactory<String, BookingEvent> factory;
    @Autowired
    SeatRepository repository;

    @Test
    @Order(1)
    void eventAccept() throws Exception {
        BookingEvent event = new BookingEvent(1L, 1L, 2, 100);
        SendResult<String, BookingEvent> r = template.send("bookings-new", event.getId(), event)
                .get(1000, TimeUnit.MILLISECONDS);
        LOG.info("Sent: {}", r.getProducerRecord().value());

        template.setConsumerFactory(factory);
        ConsumerRecord<String, BookingEvent> rec = template.receive("seat-reservation-responses", 0, 0, Duration.ofSeconds(5));
        assertNotNull(rec);
        assertNotNull(rec.value());
        assertEquals(BookingStatus.ACCEPTED, rec.value().getStatus());

        seat = repository.findById(1L).orElseThrow();
    }

    @Test
    @Order(2)
    void eventReject() throws Exception {
        BookingEvent event = new BookingEvent(2L, 2L, 99999, 100);
        template.send("bookings-new", event.getId(), event).get(1000, TimeUnit.MILLISECONDS);

        template.setConsumerFactory(factory);
        ConsumerRecord<String, BookingEvent> rec = template.receive("seat-reservation-responses", 0, 1, Duration.ofSeconds(5));
        assertNotNull(rec);
        assertNotNull(rec.value());
        assertEquals(BookingStatus.REJECTED, rec.value().getStatus());
    }

    @Test
    @Order(3)
    void eventConfirm() throws Exception {
        BookingEvent event = new BookingEvent(seat.getId().toString(), 1L, 1L, BookingStatus.CONFIRMED);
        event.setSeatCount(2);
        event.setAmount(100);
        template.send("bookings-results", event.getId(), event).get(1000, TimeUnit.MILLISECONDS);

        Thread.sleep(3000);
        Seat s = repository.findById(1L).orElseThrow();
        assertEquals(seat.getSeatsAvailable(), s.getSeatsAvailable());
        assertEquals(0, s.getSeatsReserved());
    }
}
