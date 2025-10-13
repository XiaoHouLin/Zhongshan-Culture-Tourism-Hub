package com.example.medicine_rear.pack.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {
    private String mno;
    private Integer purchaseNum;
    private Double price;
    private String mname;
}
