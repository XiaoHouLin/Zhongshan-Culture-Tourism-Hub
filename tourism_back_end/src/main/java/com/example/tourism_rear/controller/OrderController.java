package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.pack.utils.OrderInfo;
import com.example.tourism_rear.pack.utils.OrderInfoAddDate;
import com.example.tourism_rear.pack.utils.OrderRequest;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.OrderService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/add")
    public Result addOrder(@RequestBody OrderRequest orderRequest) {
        LocalDateTime localDateTime = LocalDateTime.now();
        log.info("添加订单/购物车，用户id为：{}，总价为{}，订单为：{}", orderRequest.getUid(),
                orderRequest.getTotalProductPrice(), orderRequest.getShoppingCart());
        orderService.addOrder(orderRequest, localDateTime);
        return Result.success(orderRequest.getOrderId());
    }

    @GetMapping("/getOrderByOrderId={orderId}")
    public Result listOrder(@PathVariable Integer orderId) {
        log.info("查询订单信息,订单号为{}", orderId);
        return Result.success(orderService.listOrder(orderId));
    }

    @GetMapping("/getOrderByUID={uid}")
    public Result listOrders(@PathVariable Integer uid) {
        log.info("查询订单信息,用户id为{}", uid);
        List<OrderInfoAddDate> orders = orderService.listOrders(uid);
        return Result.success(orders);
    }

    @GetMapping("/getOrderDetailByOrderId={orderId}")
    public Result listOrderDetails(@PathVariable Integer orderId) {
        log.info("查询订单详情，订单号为{}", orderId);
        return Result.success(orderService.getOrderDetails(orderId));
    }

    @GetMapping("/getOrderInfoBy={uid}")
    public Result getTotalPrice(@PathVariable Integer uid) {
        log.info("查询订单总价，未完成订单数，订单用户uid为：{}", uid);
        Map<String, Object> orderInfo = new HashMap<>();
        boolean inComplete = false;
        orderInfo.put("orderId", orderService.getOrderNumber(uid, inComplete));
        orderInfo.put("totalPrice", orderService.getTotalPrice(uid));
        return Result.success(orderInfo);
    }

    @PostMapping("/update")
    public Result updateOrder(@RequestBody OrderRequest order) {
        log.info("修改订单信息，订单id为：{}，订单信息为：{}", order.getOrderId(), order);
        orderService.updateOrder(order);
        return Result.success();
    }

    @PostMapping("/payment")
    public Result payment(@RequestBody OrderInfo order) {
        log.info("支付订单，用户信息{}", order);
        try {
            return Result.success(orderService.payOrder(order));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/pickup/verify")
    public Result verifyPickup(@RequestParam String token, HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.contains("Bearer ")) {
            return Result.error("无权限");
        }
        jwt = jwt.substring(7);
        try {
            Claims claims = Jwt.checkToken(jwt);
            Object role = claims.get("authority");
            if (role == null || Integer.parseInt(role.toString()) != 1) {
                return Result.error("无权限");
            }
        } catch (Exception e) {
            return Result.error("无权限");
        }
        try {
            String result = orderService.verifyPickup(token);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
