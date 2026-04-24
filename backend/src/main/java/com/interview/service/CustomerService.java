package com.interview.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.interview.dto.CustomerCriteria;
import com.interview.dto.CustomerRequest;
import com.interview.dto.CustomerResponse;
import com.interview.dto.VehicleResponse;
import com.interview.entity.Customer;
import com.interview.entity.CustomerStatus;
import com.interview.exception.ResourceNotFoundException;
import com.interview.repository.CustomerRepository;
import com.interview.repository.CustomerSpecifications;
import com.interview.repository.VehicleRepository;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for customer CRUD operations, delegating persistence to
 * {@link CustomerRepository} and handling entity/DTO conversion.
 */
@Service
@Slf4j
public class CustomerService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "lastName", "firstName", "createdAt", "modifiedAt");

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("lastName"),
            Sort.Order.asc("firstName"));

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    public CustomerService(CustomerRepository customerRepository,
                           VehicleRepository vehicleRepository) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
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

    public Page<CustomerResponse> findAll(CustomerCriteria criteria) {
        Specification<Customer> spec = Specification
                .where(CustomerSpecifications.hasSearchTerm(criteria.getSearch()))
                .and(CustomerSpecifications.hasStatus(criteria.getStatus()));

        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size, resolveSort(criteria));

        return customerRepository.findAll(spec, pageable).map(CustomerResponse::fromEntity);
    }

    private Sort resolveSort(CustomerCriteria criteria) {
        if (criteria.getSortBy() == null || criteria.getSortBy().trim().isEmpty()) {
            return DEFAULT_SORT;
        }
        if (!SORTABLE_FIELDS.contains(criteria.getSortBy())) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + criteria.getSortBy()
                            + ". Allowed values: " + SORTABLE_FIELDS);
        }
        Sort.Direction direction = criteria.getSortDirection() == null
                ? Sort.Direction.ASC
                : Sort.Direction.fromString(criteria.getSortDirection());
        return Sort.by(direction, criteria.getSortBy());
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

    public List<VehicleResponse> findVehiclesForCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }

        return vehicleRepository.findByCustomerIdOrderByYearDesc(customerId).stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
