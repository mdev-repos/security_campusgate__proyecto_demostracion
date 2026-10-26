package com.mdev.campusgate.utils;

import com.mdev.campusgate.exception.InvalidRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageableValidator {
    private PageableValidator() {}

    public static void validateSort(Pageable pageable, Set<String> allowedProperties) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new InvalidRequestException("Invalid sort property");
            }
        }
    }
}