package com.interview.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.interview.entity.Customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound payload for creating or updating a customer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    /**
     * Helper method for converting between DTO and the DB backed entity.
     * 
     * @return {@link Customer}
     */
    public Customer toEntity() {
        return Customer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .email(email)
                .build();
    }

    /**
     * Helper method for applying this request's values to the supplied {@link Customer}.
     * 
     * @param customer the {@link Customer} entity to aplly this request's changes to
     */
    public void applyTo(Customer customer) {
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPhone(phone);
        customer.setEmail(email);
    }
}
