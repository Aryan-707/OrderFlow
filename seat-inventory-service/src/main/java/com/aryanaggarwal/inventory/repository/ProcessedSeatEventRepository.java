package com.aryanaggarwal.inventory.repository;

import org.springframework.data.repository.CrudRepository;
import com.aryanaggarwal.inventory.domain.ProcessedSeatEvent;

public interface ProcessedSeatEventRepository extends CrudRepository<ProcessedSeatEvent, String> {
}
