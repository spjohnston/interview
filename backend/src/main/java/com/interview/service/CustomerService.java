package com.interview.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.interview.dto.CustomerRequest;
import com.interview.dto.CustomerResponse;
import com.interview.entity.Customer;
import com.interview.entity.CustomerStatus;
import com.interview.exception.ResourceNotFoundException;
import com.interview.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for customer CRUD operations, delegating persistence to
 * {@link CustomerRepository} and handling entity/DTO conversion.
 */
@Service
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse create(CustomerRequest request) {
        Customer customer = request.toEntity();
        Customer saved = customerRepository.save(customer);
        log.info("Created customer {}", saved.getId());

        return CustomerResponse.fromEntity(saved);
    }

    public CustomerResponse findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        return CustomerResponse.fromEntity(customer);
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        // update the entity with the incoming request values
        request.applyTo(customer);

        // save the updates and then convert/return a customer response
        Customer saved = customerRepository.save(customer);
        log.info("Updated customer {}", saved.getId());

        return CustomerResponse.fromEntity(saved);
    }

    public CustomerResponse updateStatus(Long id, CustomerStatus newStatus) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        customer.setStatus(newStatus);
        Customer saved = customerRepository.save(customer);
        log.info("Updated customer {} status to {}", saved.getId(), newStatus);

        return CustomerResponse.fromEntity(saved);
    }

    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }

        customerRepository.deleteById(id);
        log.info("Deleted customer {}", id);
    }
}
