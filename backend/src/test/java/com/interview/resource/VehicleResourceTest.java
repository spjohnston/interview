package com.interview.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.dto.VehicleActiveRequest;
import com.interview.dto.VehicleRequest;
import com.interview.dto.VehicleResponse;
import com.interview.exception.ResourceNotFoundException;
import com.interview.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link VehicleResource}.
 */
@WebMvcTest(VehicleResource.class)
class VehicleResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    @Test
    void testGet_Success() throws Exception {
        when(vehicleService.findById(7L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/vehicles/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value("1HGBH41JXMN000001"));
    }

    @Test
    void testGet_NotFound() throws Exception {
        when(vehicleService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Vehicle not found: 99"));

        mockMvc.perform(get("/api/vehicles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPut_Success() throws Exception {
        when(vehicleService.update(eq(7L), any())).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/vehicles/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void testPut_NotFound() throws Exception {
        when(vehicleService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Vehicle not found: 99"));

        mockMvc.perform(put("/api/vehicles/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPatchActive_Success() throws Exception {
        VehicleResponse deactivated = sampleResponse();
        deactivated.setActive(false);
        when(vehicleService.updateActive(7L, false)).thenReturn(deactivated);

        VehicleActiveRequest request = VehicleActiveRequest.builder().active(false).build();

        mockMvc.perform(patch("/api/vehicles/7/active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void testPatchActive_MissingActiveFailure() throws Exception {
        mockMvc.perform(patch("/api/vehicles/7/active")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.active").exists());
    }

    @Test
    void testDelete_Success() throws Exception {
        doNothing().when(vehicleService).delete(7L);

        mockMvc.perform(delete("/api/vehicles/7"))
                .andExpect(status().isNoContent());

        verify(vehicleService).delete(7L);
    }

    @Test
    void testDelete_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Vehicle not found: 99"))
                .when(vehicleService).delete(99L);

        mockMvc.perform(delete("/api/vehicles/99"))
                .andExpect(status().isNotFound());
    }

    private static VehicleRequest validRequest() {
        return VehicleRequest.builder()
                .vin("1HGBH41JXMN000001")
                .make("Honda")
                .model("Civic")
                .year(2020)
                .build();
    }

    private static VehicleResponse sampleResponse() {
        return VehicleResponse.builder()
                .id(7L)
                .vin("1HGBH41JXMN000001")
                .make("Honda")
                .model("Civic")
                .year(2020)
                .customerId(42L)
                .active(true)
                .build();
    }
}
