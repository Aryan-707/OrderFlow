package com.aryanaggarwal.base.domain;

/**
 * Represents every possible state a booking can be in during the SAGA lifecycle.
 * Using an enum instead of raw strings prevents typo-based silent failures
 * and makes invalid state transitions a compile-time error.
 */
public enum BookingStatus {
    NEW,                // booking just created, not yet processed
    ACCEPTED,           // individual service accepted the reservation
    PAYMENT_PENDING,    // waiting for payment service response
    SEAT_PENDING,       // waiting for seat inventory response
    CONFIRMED,          // both services accepted — saga complete
    ROLLBACK,           // one service rejected — compensating transaction needed
    REJECTED,           // both services rejected — no compensation needed
    EXPIRED             // booking timed out before saga completed
}
