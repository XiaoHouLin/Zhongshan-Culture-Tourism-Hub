package com.example.tourism_rear.pack.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderRequest {
    private Integer orderId;
    private Integer uid;
    private Double totalProductPrice;
    private List<Order> shoppingCart;

}
