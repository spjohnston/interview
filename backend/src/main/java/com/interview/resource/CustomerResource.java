package com.interview.resource;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interview.dto.CustomerCriteria;
import com.interview.dto.CustomerRequest;
import com.interview.dto.CustomerResponse;
import com.interview.dto.CustomerStatusRequest;
import com.interview.dto.VehicleRequest;
import com.interview.dto.VehicleResponse;
import com.interview.service.CustomerService;
import com.interview.service.VehicleService;

import java.util.List;

/**
 * REST controller exposing CRUD endpoints for customers under {@code /api/customers}.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerResource {

    private final CustomerService customerService;
    private final VehicleService vehicleService;

    public CustomerResource(CustomerService customerService, VehicleService vehicleService) {
        this.customerService = customerService;
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(customerService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @GetMapping("/{id}/vehicles")
    public ResponseEntity<List<VehicleResponse>> listVehicles(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.findByCustomerId(id));
    }

    @PostMapping("/{id}/vehicles")
    public ResponseEntity<VehicleResponse> createVehicle(@PathVariable Long id,
                                                         @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vehicleService.create(id, request));
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> list(CustomerCriteria criteria) {
        return ResponseEntity.ok(customerService.findAll(criteria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CustomerResponse> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody CustomerStatusRequest request) {
        return ResponseEntity.ok(customerService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
