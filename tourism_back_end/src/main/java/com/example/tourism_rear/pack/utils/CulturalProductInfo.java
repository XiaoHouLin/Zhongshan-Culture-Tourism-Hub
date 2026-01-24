package com.example.tourism_rear.pack.utils;

import lombok.Data;

/**
 * Cultural creative product information transferred between layers.
 */
@Data
public class CulturalProductInfo {
    private String productCode;
    private String productName;
    private String displayMode;
    private String highlight;
    private String category;
    private Double price;
    private Integer stock;
    private Integer sales;
}
