package com.aryanaggarwal.inventory;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.inventory.domain.Seat;
import com.aryanaggarwal.inventory.repository.SeatRepository;
import com.aryanaggarwal.inventory.service.SagaCoordinator;

@SpringBootApplication
public class SeatInventoryApp {

    private static final Logger LOG = LoggerFactory.getLogger(SeatInventoryApp.class);

    public static void main(String[] args) {
        SpringApplication.run(SeatInventoryApp.class, args);
    }

    @Autowired
    SagaCoordinator sagaCoordinator;

    @KafkaListener(id = "seat-new", topics = "bookings-new", groupId = "seat-inventory")
    public void onNewBooking(BookingEvent event) {
        LOG.info("Received new booking: {}", event);
        sagaCoordinator.reserve(event);
    }

    @KafkaListener(id = "seat-results", topics = "bookings-results", groupId = "seat-inventory")
    public void onBookingResult(BookingEvent event) {
        LOG.info("Received booking result: {}", event);
        if (event.getStatus() == BookingStatus.CONFIRMED ||
                event.getStatus() == BookingStatus.ROLLBACK) {
            sagaCoordinator.confirm(event);
        }
    }

    @Autowired
    private SeatRepository repository;

    @PostConstruct
    public void seedSeats() {
        String[] events = {"Arena Concert", "Stadium Show", "Theatre Play",
                "Comedy Night", "Jazz Evening", "Rock Festival",
                "Orchestra Night", "Dance Recital", "Film Premiere", "Poetry Slam"};
        int seatId = 1;
        for (String eventName : events) {
            for (int section = 1; section <= 10; section++) {
                int available = 50 + (seatId * 7) % 200; // deterministic varied availability
                Seat seat = new Seat(null, eventName + " - Section " + section, available, 0);
                repository.save(seat);
                seatId++;
            }
        }
        LOG.info("Seeded {} seat blocks across {} events", seatId - 1, events.length);
    }
}
