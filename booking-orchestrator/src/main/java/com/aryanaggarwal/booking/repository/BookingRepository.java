package com.aryanaggarwal.booking.repository;

import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.booking.domain.Booking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends CrudRepository<Booking, String> {

    /** Finds bookings stuck in intermediate states past their expiry time */
    @Query("SELECT b FROM Booking b WHERE b.status IN :statuses AND b.expiresAt < :now")
    List<Booking> findExpiredBookings(
            @Param("statuses") List<BookingStatus> statuses,
            @Param("now") LocalDateTime now);
}
