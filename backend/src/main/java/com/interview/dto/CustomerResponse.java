package com.interview.dto;

import java.time.LocalDateTime;

import com.interview.entity.Customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outbound representation of a customer returned by the API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    /**
     * Helper method to build a response payload from the supplied {@link Customer} entity.
     * 
     * @param customer the {@link Customer} to build the payload from
     * @return the corresponding response 
     */
    public static CustomerResponse fromEntity(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .createdAt(customer.getCreatedAt())
                .modifiedAt(customer.getModifiedAt())
                .build();
    }
}
