package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.pack.utils.OrderInfo;
import com.example.tourism_rear.pack.utils.OrderInfoAddDate;
import com.example.tourism_rear.pack.utils.OrderRequest;
import com.example.tourism_rear.pack.utils.OrderStatusUpdate;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.OrderService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
        log.info("??????????????????uid??{}", uid);
        Map<String, Object> orderInfo = new HashMap<>();
        OrderInfoAddDate activeOrder = orderService.getActiveOrderByUid(uid);
        if (activeOrder != null && activeOrder.getOrderId() != null) {
            orderInfo.put("orderId", List.of(activeOrder.getOrderId()));
            orderInfo.put("totalPrice", activeOrder.getTotalPrice() == null ? 0 : activeOrder.getTotalPrice());
            LocalDateTime expireAt = activeOrder.getCreateDate() == null
                    ? null
                    : activeOrder.getCreateDate().plusMinutes(15);
            if (expireAt != null) {
                long remainingSeconds = Math.max(0, java.time.Duration.between(LocalDateTime.now(), expireAt).getSeconds());
                orderInfo.put("remainingSeconds", remainingSeconds);
                orderInfo.put("expireTime", expireAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                orderInfo.put("remainingSeconds", 0);
                orderInfo.put("expireTime", null);
            }
        } else {
            orderInfo.put("orderId", new ArrayList<>());
            orderInfo.put("totalPrice", 0);
            orderInfo.put("remainingSeconds", 0);
            orderInfo.put("expireTime", null);
        }
        return Result.success(orderInfo);
    }

    @PostMapping("/update")
    public Result updateOrder(@RequestBody OrderRequest order) {
        log.info("?????????id??{}???????{}", order.getOrderId(), order);
        try {
            orderService.updateOrder(order);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
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

    @GetMapping("/pickup/info")
    public Result pickupInfo(@RequestParam String token, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限");
        }
        try {
            return Result.success(orderService.getPickupInfo(token));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/pickup/confirm")
    public Result pickupConfirm(@RequestParam String token, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限");
        }
        try {
            String result = orderService.confirmPickup(token);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    public Result listAllOrders(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限");
        }
        return Result.success(orderService.listAllOrders());
    }

    @PostMapping("/admin/status")
    public Result updateOrderStatus(@RequestBody OrderStatusUpdate update, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限");
        }
        try {
            orderService.updateOrderStatus(update);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/close")
    public Result closeOrder(@RequestBody OrderStatusUpdate update, HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("无权限");
        }
        try {
            orderService.closeOrder(update.getOrderId(), uid, isAdmin(request));
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result deleteOrder(@RequestBody List<Integer> orderIds, HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("无权限");
        }
        try {
            orderService.deleteOrders(orderIds, uid, isAdmin(request));
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.contains("Bearer ")) {
            return false;
        }
        jwt = jwt.substring(7);
        try {
            Claims claims = Jwt.checkToken(jwt);
            Object role = claims.get("authority");
            return role != null && Integer.parseInt(role.toString()) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private Integer getUidFromRequest(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.contains("Bearer ")) {
            return null;
        }
        jwt = jwt.substring(7);
        try {
            Claims claims = Jwt.checkToken(jwt);
            Object uid = claims.get("uid");
            return uid == null ? null : Integer.parseInt(uid.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
