package com.example.tourism_rear.service;

import com.example.tourism_rear.pack.utils.Order;
import com.example.tourism_rear.pack.utils.OrderInfo;
import com.example.tourism_rear.pack.utils.OrderInfoAddDate;
import com.example.tourism_rear.pack.utils.OrderRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {
    void addOrder(OrderRequest order, LocalDateTime localDateTime);

    List<Order> listOrder(Integer orderId);

    List<OrderInfoAddDate> listOrders(Integer uid);

    List<Order> getOrderDetails(Integer orderId);

    Double getTotalPrice(Integer uid);

    List<Integer> getOrderNumber(Integer uid, boolean status);

    void updateOrder(OrderRequest order);

    void deleteOrder(List<Integer> orderId);

    Map<String, String> payOrder(OrderInfo order);

    String verifyPickup(String token);
}
