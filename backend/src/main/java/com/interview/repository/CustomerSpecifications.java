package com.interview.repository;

import com.interview.entity.Customer;
import com.interview.entity.CustomerStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable {@link Specification} factories for {@link Customer} queries.
 */
public final class CustomerSpecifications {

    private CustomerSpecifications() {
    }

    public static Specification<Customer> hasSearchTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            return null;
        }
        String pattern = "%" + term.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("phone")), pattern),
                cb.like(cb.lower(root.get("email")), pattern)
        );
    }

    public static Specification<Customer> hasStatus(CustomerStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
