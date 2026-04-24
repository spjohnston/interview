package com.interview.repository;

import com.interview.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Vehicle} persistence.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByCustomerIdOrderByYearDesc(Long customerId);
}
