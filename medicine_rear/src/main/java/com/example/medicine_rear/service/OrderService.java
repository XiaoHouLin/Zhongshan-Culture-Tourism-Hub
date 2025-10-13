package com.example.medicine_rear.service;

import com.example.medicine_rear.pack.utils.Order;
import com.example.medicine_rear.pack.utils.OrderInfo;
import com.example.medicine_rear.pack.utils.OrderInfoAddDate;
import com.example.medicine_rear.pack.utils.OrderRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface OrderService {
    /**
     * 添加订单/购物车
     *
     * @author : 🐒
     */
    void addOrder(OrderRequest order, LocalDateTime localDateTime);

    /**
     * 根据orderId查询订单列表
     *
     * @author : 🐒
     */
    List<Order> listOrder(Integer orderId);

    /**
     * 根据UID查询订单列表
     *
     * @author : 🐒
     */
    List<OrderInfoAddDate> listOrders(Integer uid);

    /**
     * 根据订单ID查询订单详情
     *
     * @author : 🐒
     **/
    List<Order> getOrderDetails(Integer orderId);

    /**
     * 查询用户购物车中商品总价
     *
     * @author : 🐒
     */
    Double getTotalPrice(Integer uid);

    /**
     * 查询订单数,当status为true时查询已完成订单，为false时查询未完成订单
     *
     * @author : 🐒
     */
    List<Integer> getOrderNumber(Integer uid, boolean status);

    /**
     * 修改订单状态
     *
     * @author : 🐒
     */
    void updateOrder(OrderRequest order);

    /**
     * 删除订单
     *
     * @author : 🐒
     **/
    void deleteOrder(List<Integer> orderId);

    /**
     * 支付订单
     *
     * @author : 🐒
     **/
    void payOrder(OrderInfo order);
}
