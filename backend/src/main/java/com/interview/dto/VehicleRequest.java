package com.interview.dto;

import com.interview.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Inbound payload for creating or updating a vehicle. Vehicle ownership is established
 * via the create URL path and is not transferable through this payload.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRequest {

    @NotBlank
    @Size(min = 17, max = 17)
    private String vin;

    @NotBlank
    @Size(max = 50)
    private String make;

    @NotBlank
    @Size(max = 50)
    private String model;

    @NotNull
    @Min(1900)
    @Max(2100)
    private Integer year;

    public Vehicle toEntity() {
        return Vehicle.builder()
                .vin(vin)
                .make(make)
                .model(model)
                .year(year)
                .build();
    }

    public void applyTo(Vehicle vehicle) {
        vehicle.setVin(vin);
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setYear(year);
    }
}
