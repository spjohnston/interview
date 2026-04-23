package com.interview.service;

import com.interview.dto.CustomerRequest;
import com.interview.dto.CustomerResponse;
import com.interview.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service layer for customer CRUD operations. Methods are stubbed pending
 * the repository implementation.
 */
@Service
public class CustomerService {

    public CustomerResponse create(CustomerRequest request) {
        return null;
    }

    public CustomerResponse findById(Long id) {
        throw new ResourceNotFoundException("Customer not found: " + id);
    }

    public List<CustomerResponse> findAll() {
        return Collections.emptyList();
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        throw new ResourceNotFoundException("Customer not found: " + id);
    }

    public void delete(Long id) {
        throw new ResourceNotFoundException("Customer not found: " + id);
    }
}
