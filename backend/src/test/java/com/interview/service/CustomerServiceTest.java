package com.interview.service;

import com.interview.dto.CustomerRequest;
import com.interview.dto.CustomerResponse;
import com.interview.entity.Customer;
import com.interview.exception.ResourceNotFoundException;
import com.interview.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CustomerService}.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void testCreate_Success() {
        CustomerRequest request = sampleRequest();
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> {
                    Customer toSave = invocation.getArgument(0);
                    toSave.setId(42L);
                    return toSave;
                });

        CustomerResponse response = customerService.create(request);

        assertEquals(42L, response.getId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("jane@example.com", response.getEmail());
    }

    @Test
    void testFindById_Success() {
        when(customerRepository.findById(42L)).thenReturn(Optional.of(sampleCustomer()));

        CustomerResponse response = customerService.findById(42L);

        assertEquals(42L, response.getId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("jane@example.com", response.getEmail());
    }

    @Test
    void testFindById_NotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.findById(99L));
    }

    @Test
    void testFindAll_Success() {
        Customer a = sampleCustomer();
        Customer b = sampleCustomer();
        b.setId(43L);
        b.setEmail("other@example.com");
        when(customerRepository.findAll()).thenReturn(Arrays.asList(a, b));

        List<CustomerResponse> result = customerService.findAll();

        assertEquals(2, result.size());
        assertEquals(42L, result.get(0).getId());
        assertEquals(43L, result.get(1).getId());
    }

    @Test
    void testUpdate_Success() {
        Customer existing = sampleCustomer();
        when(customerRepository.findById(42L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerRequest request = sampleRequest();
        request.setFirstName("Janet");
        CustomerResponse response = customerService.update(42L, request);

        assertEquals("Janet", response.getFirstName());
    }

    @Test
    void testUpdate_NotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.update(99L, sampleRequest()));
    }

    @Test
    void testDelete_Success() {
        when(customerRepository.existsById(42L)).thenReturn(true);

        customerService.delete(42L);

        verify(customerRepository).deleteById(42L);
    }

    @Test
    void testDelete_NotFound() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> customerService.delete(99L));
        verify(customerRepository, never()).deleteById(any());
    }

    private static CustomerRequest sampleRequest() {
        return CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .phone("555-1234")
                .email("jane@example.com")
                .build();
    }

    private static Customer sampleCustomer() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 23, 10, 30);
        return Customer.builder()
                .id(42L)
                .firstName("Jane")
                .lastName("Doe")
                .phone("555-1234")
                .email("jane@example.com")
                .createdAt(now)
                .modifiedAt(now)
                .build();
    }
}
