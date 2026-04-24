package com.interview.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.dto.CustomerCriteria;
import com.interview.dto.CustomerRequest;
import com.interview.dto.CustomerResponse;
import com.interview.dto.CustomerStatusRequest;
import com.interview.dto.VehicleResponse;
import com.interview.entity.CustomerStatus;
import com.interview.exception.ResourceNotFoundException;
import com.interview.service.CustomerService;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link CustomerResource}.
 */
@WebMvcTest(CustomerResource.class)
class CustomerResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    void testPost_Success() throws Exception {
        when(customerService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("555-1234"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.modifiedAt").exists());
    }

    @Test
    void testPost_BlankValidationFailure() throws Exception {
        CustomerRequest request = new CustomerRequest();

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.lastName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void testPost_EmailValidationFailure() throws Exception {
        CustomerRequest request = validRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void testPost_MalformedJsonFailure() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    @Test
    void testGetById_Success() throws Exception {
        when(customerService.findById(42L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/customers/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testGetVehicles_Success() throws Exception {
        VehicleResponse v = VehicleResponse.builder()
                .id(1L)
                .vin("1HGBH41JXMN000001")
                .make("Honda")
                .model("Civic")
                .year(2020)
                .customerId(42L)
                .active(true)
                .build();
        when(customerService.findVehiclesForCustomer(42L)).thenReturn(List.of(v));

        mockMvc.perform(get("/api/customers/42/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vin").value("1HGBH41JXMN000001"))
                .andExpect(jsonPath("$[0].customerId").value(42))
                .andExpect(jsonPath("$[0].make").value("Honda"));
    }

    @Test
    void testGetVehicles_CustomerNotFound() throws Exception {
        when(customerService.findVehiclesForCustomer(99L))
                .thenThrow(new ResourceNotFoundException("Customer not found: 99"));

        mockMvc.perform(get("/api/customers/99/vehicles"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetById_NotFound() throws Exception {
        when(customerService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Customer not found: 99"));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Customer not found: 99"));
    }

    @Test
    void testGetList_Success() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(
                Collections.singletonList(sampleResponse()),
                PageRequest.of(0, 20),
                1);
        when(customerService.findAll(any(CustomerCriteria.class))).thenReturn(page);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(42))
                .andExpect(jsonPath("$.content[0].firstName").value("Jane"))
                .andExpect(jsonPath("$.content[0].email").value("jane@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetList_Paged() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(
                Collections.singletonList(sampleResponse()),
                PageRequest.of(0, 10),
                1);
        when(customerService.findAll(any(CustomerCriteria.class))).thenReturn(page);

        mockMvc.perform(get("/api/customers?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetList_FilterByStatus() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(
                Collections.singletonList(sampleResponse()),
                PageRequest.of(0, 20),
                1);
        when(customerService.findAll(any(CustomerCriteria.class))).thenReturn(page);

        mockMvc.perform(get("/api/customers?status=INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void testGetList_InvalidStatusFailure() throws Exception {
        mockMvc.perform(get("/api/customers?status=DELETED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetList_InvalidSortValidationFailure() throws Exception {
        when(customerService.findAll(any(CustomerCriteria.class)))
                .thenThrow(new IllegalArgumentException("Invalid sort field: bogus"));

        mockMvc.perform(get("/api/customers?sortBy=bogus"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sort field: bogus"));
    }

    @Test
    void testPut_Success() throws Exception {
        when(customerService.update(eq(42L), any())).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/customers/42")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.modifiedAt").exists());
    }

    @Test
    void testPut_NotFound() throws Exception {
        when(customerService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Customer not found: 99"));

        mockMvc.perform(put("/api/customers/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPut_BlankValidationFailure() throws Exception {
        CustomerRequest request = new CustomerRequest();

        mockMvc.perform(put("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists());
    }

    @Test
    void testPatchStatus_Success() throws Exception {
        CustomerResponse updated = sampleResponse();
        updated.setStatus(CustomerStatus.INACTIVE);
        when(customerService.updateStatus(42L, CustomerStatus.INACTIVE)).thenReturn(updated);

        CustomerStatusRequest request = CustomerStatusRequest.builder()
                .status(CustomerStatus.INACTIVE)
                .build();

        mockMvc.perform(patch("/api/customers/42/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void testPatchStatus_NotFound() throws Exception {
        when(customerService.updateStatus(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Customer not found: 99"));

        CustomerStatusRequest request = CustomerStatusRequest.builder()
                .status(CustomerStatus.INACTIVE)
                .build();

        mockMvc.perform(patch("/api/customers/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPatchStatus_MissingStatusValidationFailure() throws Exception {
        mockMvc.perform(patch("/api/customers/42/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    @Test
    void testDelete_Success() throws Exception {
        doNothing().when(customerService).delete(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).delete(1L);
    }

    @Test
    void testDelete_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Customer not found: 99"))
                .when(customerService).delete(99L);

        mockMvc.perform(delete("/api/customers/99"))
                .andExpect(status().isNotFound());
    }

    private static CustomerRequest validRequest() {
        return CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .phone("555-1234")
                .email("jane@example.com")
                .build();
    }

    private static CustomerResponse sampleResponse() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 23, 10, 30);
        return CustomerResponse.builder()
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
