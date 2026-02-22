package com.aryanaggarwal.payment.repository;

import org.springframework.data.repository.CrudRepository;
import com.aryanaggarwal.payment.domain.Customer;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
}
