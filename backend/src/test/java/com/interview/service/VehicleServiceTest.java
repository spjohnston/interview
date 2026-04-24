package com.interview.service;

import com.interview.dto.VehicleRequest;
import com.interview.dto.VehicleResponse;
import com.interview.entity.Customer;
import com.interview.entity.Vehicle;
import com.interview.exception.ResourceNotFoundException;
import com.interview.repository.CustomerRepository;
import com.interview.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link VehicleService}.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void testCreate_Success() {
        Customer customer = sampleCustomer();
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(7L);
            return v;
        });

        VehicleResponse response = vehicleService.create(42L, sampleRequest());

        assertEquals(7L, response.getId());
        assertEquals("1HGBH41JXMN000001", response.getVin());
        assertEquals(42L, response.getCustomerId());
    }

    @Test
    void testCreate_CustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.create(99L, sampleRequest()));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void testFindById_Success() {
        when(vehicleRepository.findById(7L)).thenReturn(Optional.of(sampleVehicle()));

        VehicleResponse response = vehicleService.findById(7L);

        assertEquals("1HGBH41JXMN000001", response.getVin());
        assertEquals(42L, response.getCustomerId());
    }

    @Test
    void testFindById_NotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.findById(99L));
    }

    @Test
    void testFindByCustomerId_Success() {
        when(customerRepository.existsById(42L)).thenReturn(true);
        Vehicle v1 = sampleVehicle();
        Vehicle v2 = sampleVehicle();
        v2.setId(8L);
        v2.setVin("1HGBH41JXMN000002");
        when(vehicleRepository.findByCustomerIdAndActiveTrueOrderByYearDesc(42L))
                .thenReturn(Arrays.asList(v1, v2));

        List<VehicleResponse> result = vehicleService.findByCustomerId(42L);

        assertEquals(2, result.size());
        assertEquals("1HGBH41JXMN000001", result.get(0).getVin());
    }

    @Test
    void testFindByCustomerId_CustomerNotFound() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.findByCustomerId(99L));
    }

    @Test
    void testUpdate_Success() {
        Vehicle existing = sampleVehicle();
        when(vehicleRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleRequest request = sampleRequest();
        request.setMake("Ford");
        VehicleResponse response = vehicleService.update(7L, request);

        assertEquals("Ford", response.getMake());
    }

    @Test
    void testUpdate_NotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.update(99L, sampleRequest()));
    }

    @Test
    void testUpdateActive_Success() {
        Vehicle existing = sampleVehicle();
        when(vehicleRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleResponse response = vehicleService.updateActive(7L, false);

        assertFalse(response.isActive());
    }

    @Test
    void testUpdateActive_NotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.updateActive(99L, false));
    }

    @Test
    void testDelete_Success() {
        when(vehicleRepository.existsById(7L)).thenReturn(true);

        vehicleService.delete(7L);

        verify(vehicleRepository).deleteById(7L);
    }

    @Test
    void testDelete_NotFound() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.delete(99L));
        verify(vehicleRepository, never()).deleteById(any());
    }

    private static VehicleRequest sampleRequest() {
        return VehicleRequest.builder()
                .vin("1HGBH41JXMN000001")
                .make("Honda")
                .model("Civic")
                .year(2020)
                .build();
    }

    private static Vehicle sampleVehicle() {
        return Vehicle.builder()
                .id(7L)
                .vin("1HGBH41JXMN000001")
                .make("Honda")
                .model("Civic")
                .year(2020)
                .active(true)
                .customer(sampleCustomer())
                .build();
    }

    private static Customer sampleCustomer() {
        return Customer.builder()
                .id(42L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .build();
    }
}
