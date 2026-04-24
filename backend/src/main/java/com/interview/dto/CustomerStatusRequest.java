package com.interview.dto;

import javax.validation.constraints.NotNull;

import com.interview.entity.CustomerStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound payload for updating a customer's status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerStatusRequest {

    @NotNull
    private CustomerStatus status;
}
