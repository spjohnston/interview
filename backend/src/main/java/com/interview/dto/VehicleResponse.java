package com.interview.dto;

import com.interview.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbound representation of a vehicle returned by the API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    private Long id;
    private String vin;
    private String make;
    private String model;
    private Integer year;
    private Long customerId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .vin(vehicle.getVin())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .customerId(vehicle.getCustomer().getId())
                .active(vehicle.isActive())
                .createdAt(vehicle.getCreatedAt())
                .modifiedAt(vehicle.getModifiedAt())
                .build();
    }
}
