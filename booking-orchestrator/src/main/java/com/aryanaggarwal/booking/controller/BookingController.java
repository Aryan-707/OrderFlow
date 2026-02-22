package com.aryanaggarwal.booking.controller;

import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.booking.domain.Booking;
import com.aryanaggarwal.booking.domain.BookingStateResponse;
import com.aryanaggarwal.booking.repository.BookingRepository;
import com.aryanaggarwal.booking.service.BookingGeneratorService;
import com.aryanaggarwal.booking.service.SagaCoordinator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger LOG = LoggerFactory.getLogger(BookingController.class);

    private final KafkaTemplate<String, BookingEvent> template;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final BookingGeneratorService generatorService;
    private final BookingRepository bookingRepository;
    private final SagaCoordinator sagaCoordinator;

    @Value("${booking.expiry-seconds:300}")
    private int expirySeconds;

    public BookingController(KafkaTemplate<String, BookingEvent> template,
                             StreamsBuilderFactoryBean kafkaStreamsFactory,
                             BookingGeneratorService generatorService,
                             BookingRepository bookingRepository,
                             SagaCoordinator sagaCoordinator) {
        this.template = template;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.generatorService = generatorService;
        this.bookingRepository = bookingRepository;
        this.sagaCoordinator = sagaCoordinator;
    }

    @PostMapping
    public BookingEvent create(@RequestBody BookingEvent event) {
        // persist booking to DB before publishing to Kafka
        Booking booking = new Booking();
        booking.setId(event.getId());
        booking.setCustomerId(event.getCustomerId());
        booking.setSeatId(event.getSeatId());
        booking.setAmount(event.getAmount());
        booking.setStatus(BookingStatus.NEW);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusSeconds(expirySeconds));
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        template.send("bookings-new", event.getId(), event);
        sagaCoordinator.recordInitiated();
        LOG.info("[SAGA] Booking {} -> NEW, published to bookings-new", event.getId());
        return event;
    }

    @PostMapping("/generate")
    public boolean generate() {
        generatorService.generate();
        return true;
    }

    /** Returns the current state of a specific booking from DB */
    @GetMapping("/{id}")
    public ResponseEntity<BookingStateResponse> getState(@PathVariable String id) {
        return bookingRepository.findById(id)
                .map(b -> ResponseEntity.ok(new BookingStateResponse(
                        b.getId(), b.getStatus(), b.getCustomerId(),
                        b.getSeatId(), b.getCreatedAt(), b.getExpiresAt(), b.getUpdatedAt())))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Returns all booking events from Kafka Streams state store */
    @GetMapping
    public List<BookingEvent> all() {
        List<BookingEvent> bookings = new ArrayList<>();
        ReadOnlyKeyValueStore<String, BookingEvent> store = kafkaStreamsFactory
                .getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        "bookings",
                        QueryableStoreTypes.keyValueStore()));
        KeyValueIterator<String, BookingEvent> it = store.all();
        it.forEachRemaining(kv -> bookings.add(kv.value));
        return bookings;
    }
}
