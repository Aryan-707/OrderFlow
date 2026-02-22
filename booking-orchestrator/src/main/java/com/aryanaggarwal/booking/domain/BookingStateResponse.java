package com.aryanaggarwal.booking.domain;

import com.aryanaggarwal.base.domain.BookingStatus;
import java.time.LocalDateTime;

/** Read-only response DTO for the GET /bookings/{id} endpoint */
public record BookingStateResponse(
        String bookingId,
        BookingStatus status,
        Long customerId,
        Long seatId,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime updatedAt
) {}
