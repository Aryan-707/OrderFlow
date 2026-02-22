package com.aryanaggarwal.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.payment.domain.Customer;
import com.aryanaggarwal.payment.domain.ProcessedPaymentEvent;
import com.aryanaggarwal.payment.repository.CustomerRepository;
import com.aryanaggarwal.payment.repository.ProcessedPaymentEventRepository;

@Service
public class SagaCoordinator {

    private static final String SOURCE = "payment";
    private static final Logger LOG = LoggerFactory.getLogger(SagaCoordinator.class);

    private final CustomerRepository repository;
    private final KafkaTemplate<String, BookingEvent> template;
    private final ProcessedPaymentEventRepository processedRepo;

    public SagaCoordinator(CustomerRepository repository,
                           KafkaTemplate<String, BookingEvent> template,
                           ProcessedPaymentEventRepository processedRepo) {
        this.repository = repository;
        this.template = template;
        this.processedRepo = processedRepo;
    }

    public void reserve(BookingEvent event) {
        // idempotency check — Kafka is at-least-once delivery
        if (processedRepo.existsById(event.getId())) {
            LOG.warn("[SAGA] Duplicate payment event skipped: {}", event.getId());
            return;
        }

        Customer customer = repository.findById(event.getCustomerId()).orElseThrow();
        LOG.info("[SAGA] Booking {} -> checking customer {} balance: {}",
                event.getId(), customer.getName(), customer.getAmountAvailable());

        if (event.getAmount() < customer.getAmountAvailable()) {
            event.setStatus(BookingStatus.ACCEPTED);
            customer.setAmountReserved(customer.getAmountReserved() + event.getAmount());
            customer.setAmountAvailable(customer.getAmountAvailable() - event.getAmount());
        } else {
            event.setStatus(BookingStatus.REJECTED);
        }
        event.setSource(SOURCE);
        repository.save(customer);
        processedRepo.save(new ProcessedPaymentEvent(event.getId()));
        template.send("payment-responses", event.getId(), event);
        LOG.info("[SAGA] Booking {} -> payment: {}", event.getId(), event.getStatus());
    }

    public void confirm(BookingEvent event) {
        Customer customer = repository.findById(event.getCustomerId()).orElseThrow();
        LOG.info("[SAGA] Booking {} -> confirming for customer {}", event.getId(), customer.getName());

        if (event.getStatus() == BookingStatus.CONFIRMED) {
            // saga succeeded — commit the reservation (reduce reserved amount)
            customer.setAmountReserved(customer.getAmountReserved() - event.getAmount());
            repository.save(customer);
        } else if (event.getStatus() == BookingStatus.ROLLBACK &&
                !SOURCE.equalsIgnoreCase(event.getSource())) {
            // another service rejected — undo our reservation
            customer.setAmountReserved(customer.getAmountReserved() - event.getAmount());
            customer.setAmountAvailable(customer.getAmountAvailable() + event.getAmount());
            repository.save(customer);
            LOG.info("[SAGA] Booking {} -> payment ROLLED BACK for customer {}", event.getId(), customer.getName());
        }
    }
}
