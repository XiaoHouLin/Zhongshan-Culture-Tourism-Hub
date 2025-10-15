package com.example.tourism_rear.pack.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {
    private String productCode;
    private Integer purchaseNum;
    private Double price;
    private String productName;
}
