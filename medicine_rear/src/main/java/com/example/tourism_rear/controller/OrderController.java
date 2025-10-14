package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.OrderInfo;
import com.example.tourism_rear.pack.utils.OrderInfoAddDate;
import com.example.tourism_rear.pack.utils.OrderRequest;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 从购物车数据添加订单
     *
     * @author : 🐒
     */
    @PostMapping("/add")
    public Result addOrder(@RequestBody OrderRequest orderRequest) {
        LocalDateTime localDateTime = LocalDateTime.now();
        log.info("添加订单/购物车，用户id为：{}，总价为{}，订单为：{}", orderRequest.getUid(),
                orderRequest.getTotalProductPrice(), orderRequest.getShoppingCart());
        orderService.addOrder(orderRequest, localDateTime);
        return Result.success(orderRequest.getOrderId());
    }

    /**
     * 根据orderId查询订单信息,条件查询不写了(￣、￣)
     *
     * @author : 🐒
     */
    @GetMapping("/getOrderByOrderId={orderId}")
    public Result listOrder(@PathVariable Integer orderId) {
        log.info("查询订单信息,订单号为{}", orderId);
        return Result.success(orderService.listOrder(orderId));
    }

    /**
     * 根据UID查询订单信息
     *
     * @author : 🐒
     */
    @GetMapping("/getOrderByUID={uid}")
    public Result listOrders(@PathVariable Integer uid) {
        log.info("查询订单信息,用户id为{}", uid);
        return Result.success(orderService.listOrders(uid));
    }

    /**
     * 根据orderId查询订单详情
     *
     * @author : 🐒
     */
    @GetMapping("/getOrderDetailByOrderId={orderId}")
    public Result listOrderDetails(@PathVariable Integer orderId) {
        log.info("查询订单详情，订单号为{}", orderId);
        return Result.success(orderService.getOrderDetails(orderId));
    }

    /**
     * 查询订单总价，订单数
     *
     * @author : 🐒
     */
    @GetMapping("/getOrderInfoBy={uid}")
    public Result getTotalPrice(@PathVariable Integer uid) {
        log.info("查询订单总价，未完成订单数，订单用户uid为：{}", uid);
        Map<String, Object> orderInfo = new HashMap<>();
        boolean inComplete = false;
        orderInfo.put("orderId", orderService.getOrderNumber(uid, inComplete));
        orderInfo.put("totalPrice", orderService.getTotalPrice(uid));
        return Result.success(orderInfo);
    }

    /**
     * 修改订单信息（将旧订单全部删除，再插入新订单，简单省事）
     *
     * @author : 🐒
     */
    @PostMapping("/update")
    public Result updateOrder(@RequestBody OrderRequest order) {
        log.info("修改订单信息，订单id为：{}，订单信息为：{}", order.getOrderId(), order);
        orderService.updateOrder(order);
        return Result.success();
    }

    /**
     * 支付订单
     *
     * @author : 🐒
     */
    @PostMapping("/payment")
    public Result payment(@RequestBody OrderInfo order) {
        log.info("支付订单，用户信息{}", order);
        try {
            orderService.payOrder(order);
            return Result.success();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
