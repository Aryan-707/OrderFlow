package com.aryanaggarwal.inventory.repository;

import org.springframework.data.repository.CrudRepository;
import com.aryanaggarwal.inventory.domain.Seat;

public interface SeatRepository extends CrudRepository<Seat, Long> {
}
