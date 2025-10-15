package com.example.tourism_rear.pack.utils;

import lombok.Data;

/**
 * Aggregated statistics of cultural product purchases grouped by category.
 */
@Data
public class CulturalProductCategory {
    private String category;
    private Integer totalQuantity;
}
