package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Inbound payload for toggling a vehicle's active flag.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleActiveRequest {

    @NotNull
    private Boolean active;
}
