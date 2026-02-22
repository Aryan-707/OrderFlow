package com.aryanaggarwal.booking;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.apache.kafka.clients.admin.NewTopic;
import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.booking.domain.Booking;
import com.aryanaggarwal.booking.repository.BookingRepository;
import com.aryanaggarwal.booking.service.SagaCoordinator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableKafkaStreams
@EnableAsync
@EnableScheduling
public class BookingOrchestratorApp {

    private static final Logger LOG = LoggerFactory.getLogger(BookingOrchestratorApp.class);

    public static void main(String[] args) {
        SpringApplication.run(BookingOrchestratorApp.class, args);
    }

    @Autowired
    SagaCoordinator sagaCoordinator;

    @Autowired
    BookingRepository bookingRepository;

    // topic where new booking requests are published
    @Bean
    public NewTopic bookingsNewTopic() {
        return TopicBuilder.name("bookings-new")
                .partitions(3)
                .compact()
                .build();
    }

    // topic where final saga results land after the join
    @Bean
    public NewTopic bookingsResultsTopic() {
        return TopicBuilder.name("bookings-results")
                .partitions(3)
                .compact()
                .build();
    }

    // payment service writes its accept/reject here
    @Bean
    public NewTopic paymentResponsesTopic() {
        return TopicBuilder.name("payment-responses")
                .partitions(3)
                .compact()
                .build();
    }

    // seat inventory service writes its accept/reject here
    @Bean
    public NewTopic seatReservationResponsesTopic() {
        return TopicBuilder.name("seat-reservation-responses")
                .partitions(3)
                .compact()
                .build();
    }

    /**
     * Core SAGA join: correlates payment and seat responses within a 10-second window.
     * Both services process the booking in parallel, and this join waits for both
     * to respond before determining the final status.
     */
    @Bean
    public KStream<String, BookingEvent> stream(StreamsBuilder builder) {
        JsonSerde<BookingEvent> bookingSerde = new JsonSerde<>(BookingEvent.class);
        KStream<String, BookingEvent> stream = builder
                .stream("payment-responses", Consumed.with(Serdes.String(), bookingSerde));

        stream.join(
                        builder.stream("seat-reservation-responses",
                                Consumed.with(Serdes.String(), bookingSerde)),
                        sagaCoordinator::resolveOutcome, // determines CONFIRMED, ROLLBACK, or REJECTED
                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(10)),
                        StreamJoined.with(Serdes.String(), bookingSerde, bookingSerde))
                .peek((k, o) -> LOG.info("[SAGA] Booking {} -> FINAL: {}", k, o.getStatus()))
                .to("bookings-results");

        return stream;
    }

    /**
     * Materializes the bookings-results stream into a queryable state store.
     * This enables the GET /bookings endpoint to read current booking states
     * without needing a separate database query for Kafka-sourced data.
     */
    @Bean
    public KTable<String, BookingEvent> table(StreamsBuilder builder) {
        KeyValueBytesStoreSupplier store =
                Stores.persistentKeyValueStore("bookings");
        JsonSerde<BookingEvent> bookingSerde = new JsonSerde<>(BookingEvent.class);
        KStream<String, BookingEvent> stream = builder
                .stream("bookings-results", Consumed.with(Serdes.String(), bookingSerde));
        return stream.toTable(Materialized.<String, BookingEvent>as(store)
                .withKeySerde(Serdes.String())
                .withValueSerde(bookingSerde));
    }

    /**
     * Listens for final saga results and persists them to the database.
     * This keeps the DB in sync with Kafka so the expiry job and GET endpoint
     * can query booking state from PostgreSQL.
     */
    @KafkaListener(topics = "bookings-results", groupId = "booking-db-sync")
    public void onSagaResult(BookingEvent event) {
        bookingRepository.findById(event.getId()).ifPresent(booking -> {
            booking.setStatus(event.getStatus());
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
            LOG.info("[SAGA] Booking {} -> DB synced: {}", event.getId(), event.getStatus());
        });
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("kafkaSender-");
        executor.initialize();
        return executor;
    }
}
