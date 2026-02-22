package com.aryanaggarwal.payment.repository;

import org.springframework.data.repository.CrudRepository;
import com.aryanaggarwal.payment.domain.ProcessedPaymentEvent;

public interface ProcessedPaymentEventRepository extends CrudRepository<ProcessedPaymentEvent, String> {
}
