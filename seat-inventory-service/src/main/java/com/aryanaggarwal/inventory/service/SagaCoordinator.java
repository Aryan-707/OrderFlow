package com.aryanaggarwal.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.inventory.domain.ProcessedSeatEvent;
import com.aryanaggarwal.inventory.domain.Seat;
import com.aryanaggarwal.inventory.repository.ProcessedSeatEventRepository;
import com.aryanaggarwal.inventory.repository.SeatRepository;

@Service
public class SagaCoordinator {

    private static final String SOURCE = "seat-inventory";
    private static final Logger LOG = LoggerFactory.getLogger(SagaCoordinator.class);

    private final SeatRepository repository;
    private final KafkaTemplate<String, BookingEvent> template;
    private final ProcessedSeatEventRepository processedRepo;

    public SagaCoordinator(SeatRepository repository,
                           KafkaTemplate<String, BookingEvent> template,
                           ProcessedSeatEventRepository processedRepo) {
        this.repository = repository;
        this.template = template;
        this.processedRepo = processedRepo;
    }

    public void reserve(BookingEvent event) {
        // idempotency check — prevents double-reservation on Kafka replay
        if (processedRepo.existsById(event.getId())) {
            LOG.warn("[SAGA] Duplicate seat event skipped: {}", event.getId());
            return;
        }

        Seat seat = repository.findById(event.getSeatId()).orElseThrow();
        LOG.info("[SAGA] Booking {} -> checking seat '{}' availability: {}",
                event.getId(), seat.getName(), seat.getSeatsAvailable());

        if (event.getStatus() == BookingStatus.NEW) {
            if (event.getSeatCount() < seat.getSeatsAvailable()) {
                seat.setSeatsReserved(seat.getSeatsReserved() + event.getSeatCount());
                seat.setSeatsAvailable(seat.getSeatsAvailable() - event.getSeatCount());
                event.setStatus(BookingStatus.ACCEPTED);
                repository.save(seat);
            } else {
                event.setStatus(BookingStatus.REJECTED);
            }
            event.setSource(SOURCE);
            processedRepo.save(new ProcessedSeatEvent(event.getId()));
            template.send("seat-reservation-responses", event.getId(), event);
            LOG.info("[SAGA] Booking {} -> seat: {}", event.getId(), event.getStatus());
        }
    }

    public void confirm(BookingEvent event) {
        Seat seat = repository.findById(event.getSeatId()).orElseThrow();
        LOG.info("[SAGA] Booking {} -> confirming for seat '{}'", event.getId(), seat.getName());

        if (event.getStatus() == BookingStatus.CONFIRMED) {
            seat.setSeatsReserved(seat.getSeatsReserved() - event.getSeatCount());
            repository.save(seat);
        } else if (event.getStatus() == BookingStatus.ROLLBACK &&
                !SOURCE.equalsIgnoreCase(event.getSource())) {
            seat.setSeatsReserved(seat.getSeatsReserved() - event.getSeatCount());
            seat.setSeatsAvailable(seat.getSeatsAvailable() + event.getSeatCount());
            repository.save(seat);
            LOG.info("[SAGA] Booking {} -> seat ROLLED BACK for '{}'", event.getId(), seat.getName());
        }
    }
}
