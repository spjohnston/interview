package com.interview.repository;

import com.interview.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Customer} persistence.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
