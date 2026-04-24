package com.interview.service;

import com.interview.dto.VehicleRequest;
import com.interview.dto.VehicleResponse;
import com.interview.entity.Customer;
import com.interview.entity.Vehicle;
import com.interview.exception.ResourceNotFoundException;
import com.interview.repository.CustomerRepository;
import com.interview.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for vehicle CRUD operations. Owns entity/DTO conversion and coordinates
 * with {@link CustomerRepository} for ownership lookups.
 */
@Service
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    public VehicleService(VehicleRepository vehicleRepository,
                          CustomerRepository customerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
    }

    public VehicleResponse create(Long customerId, VehicleRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + customerId));

        Vehicle vehicle = request.toEntity();
        vehicle.setCustomer(customer);
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Created vehicle {} for customer {}", saved.getId(), customer.getId());

        return VehicleResponse.fromEntity(saved);
    }

    public VehicleResponse findById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));

        return VehicleResponse.fromEntity(vehicle);
    }

    public List<VehicleResponse> findByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }

        return vehicleRepository.findByCustomerIdAndActiveTrueOrderByYearDesc(customerId).stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));

        request.applyTo(vehicle);
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Updated vehicle {}", saved.getId());

        return VehicleResponse.fromEntity(saved);
    }

    public VehicleResponse updateActive(Long id, boolean active) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));

        vehicle.setActive(active);
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Updated vehicle {} active to {}", saved.getId(), active);

        return VehicleResponse.fromEntity(saved);
    }

    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle not found: " + id);
        }

        vehicleRepository.deleteById(id);
        log.info("Deleted vehicle {}", id);
    }
}
