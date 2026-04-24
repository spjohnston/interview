package com.interview.dto;

import com.interview.entity.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter, sort, and paging criteria for listing customers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCriteria {

    private String search;
    private CustomerStatus status;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
