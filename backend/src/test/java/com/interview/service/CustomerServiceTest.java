package com.interview.service;

import com.interview.dto.CustomerCriteria;
import com.interview.dto.CustomerRequest;
import com.interview.dto.CustomerResponse;
import com.interview.dto.VehicleResponse;
import com.interview.entity.Customer;
import com.interview.entity.CustomerStatus;
import com.interview.entity.Vehicle;
import com.interview.exception.ResourceNotFoundException;
import com.interview.repository.CustomerRepository;
import com.interview.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Mock
    private VehicleRepository vehicleRepository;

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
        when(customerRepository.findAll(Mockito.<Specification<Customer>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(a, b)));

        Page<CustomerResponse> result = customerService.findAll(new CustomerCriteria());

        assertEquals(2, result.getContent().size());
        assertEquals(42L, result.getContent().get(0).getId());
        assertEquals(43L, result.getContent().get(1).getId());
    }

    @Test
    void testFindAll_SearchPassesSpecification() {
        when(customerRepository.findAll(Mockito.<Specification<Customer>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleCustomer())));

        CustomerCriteria criteria = CustomerCriteria.builder().search("doe").build();
        Page<CustomerResponse> result = customerService.findAll(criteria);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testFindAll_Paged() {
        when(customerRepository.findAll(Mockito.<Specification<Customer>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleCustomer())));

        CustomerCriteria criteria = CustomerCriteria.builder().page(0).size(10).build();
        Page<CustomerResponse> result = customerService.findAll(criteria);

        assertEquals(1, result.getContent().size());
        assertEquals(42L, result.getContent().get(0).getId());
    }

    @Test
    void testFindAll_FilterByStatus() {
        when(customerRepository.findAll(Mockito.<Specification<Customer>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleCustomer())));

        CustomerCriteria criteria = CustomerCriteria.builder().status(CustomerStatus.INACTIVE).build();
        Page<CustomerResponse> result = customerService.findAll(criteria);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testFindAll_InvalidSortValidationFailure() {
        CustomerCriteria criteria = CustomerCriteria.builder().sortBy("bogus").build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.findAll(criteria));
        assertTrue(ex.getMessage().contains("bogus"));
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
    void testUpdateStatus_Success() {
        Customer existing = sampleCustomer();
        when(customerRepository.findById(42L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerResponse response = customerService.updateStatus(42L, CustomerStatus.INACTIVE);

        assertEquals(CustomerStatus.INACTIVE, response.getStatus());
    }

    @Test
    void testUpdateStatus_NotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.updateStatus(99L, CustomerStatus.INACTIVE));
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

    @Test
    void testFindVehiclesForCustomer_Success() {
        when(customerRepository.existsById(42L)).thenReturn(true);
        Vehicle v1 = sampleVehicle();
        Vehicle v2 = sampleVehicle();
        v2.setId(2L);
        v2.setVin("1HGBH41JXMN000002");
        when(vehicleRepository.findByCustomerIdOrderByYearDesc(42L))
                .thenReturn(List.of(v1, v2));

        List<VehicleResponse> result = customerService.findVehiclesForCustomer(42L);

        assertEquals(2, result.size());
        assertEquals("1HGBH41JXMN000001", result.get(0).getVin());
        assertEquals(42L, result.get(0).getCustomerId());
    }

    @Test
    void testFindVehiclesForCustomer_CustomerNotFound() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.findVehiclesForCustomer(99L));
    }

    private static Vehicle sampleVehicle() {
        return Vehicle.builder()
                .id(1L)
                .vin("1HGBH41JXMN000001")
                .make("Honda")
                .model("Civic")
                .year(2020)
                .customer(sampleCustomer())
                .build();
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
                .status(CustomerStatus.ACTIVE)
                .createdAt(now)
                .modifiedAt(now)
                .build();
    }
}
