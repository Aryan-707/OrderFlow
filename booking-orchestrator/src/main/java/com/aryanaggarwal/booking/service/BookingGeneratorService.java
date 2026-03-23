package com.aryanaggarwal.booking.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.aryanaggarwal.base.domain.BookingEvent;

import java.util.Random;

@Service
public class BookingGeneratorService {

    private static final Random RAND = new Random();
    private final KafkaTemplate<String, BookingEvent> template;

    public BookingGeneratorService(KafkaTemplate<String, BookingEvent> template) {
        this.template = template;
    }

    /** Generates test bookings asynchronously to avoid blocking the HTTP thread */
    @Async("taskExecutor")
    public void generate() {
        for (int i = 0; i < 10000; i++) {
            int multiplier = RAND.nextInt(5) + 1;
            BookingEvent event = new BookingEvent(
                    RAND.nextLong(100) + 1,   // customerId between 1-100
                    RAND.nextLong(100) + 1,   // seatId between 1-100
                    multiplier,                // seat count
                    100 * multiplier           // amount proportional to seats
            );
            template.send("bookings-new", event.getId(), event);
        }
    }
}
