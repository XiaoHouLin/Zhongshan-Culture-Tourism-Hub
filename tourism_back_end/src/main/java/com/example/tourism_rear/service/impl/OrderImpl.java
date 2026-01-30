package com.example.tourism_rear.service.impl;

import com.example.tourism_rear.mapper.CulturalProductMapper;
import com.example.tourism_rear.mapper.OrderMapper;
import com.example.tourism_rear.mapper.UserMapper;
import com.example.tourism_rear.pack.utils.Order;
import com.example.tourism_rear.pack.utils.OrderInfo;
import com.example.tourism_rear.pack.utils.OrderInfoAddDate;
import com.example.tourism_rear.pack.utils.OrderRequest;
import com.example.tourism_rear.pack.utils.PickupTokenUtil;
import com.example.tourism_rear.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CulturalProductMapper productMapper;
    @Autowired
    private PickupTokenUtil pickupTokenUtil;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 1000;
    private static final int EXEMPT_CARRIAGE = 60;
    private static final int CARRIAGE_PRICE = 10;
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final java.security.SecureRandom random = new java.security.SecureRandom();

    @Transactional
    @Override
    public void addOrder(OrderRequest order, LocalDateTime localDateTime) {
        orderMapper.addOrder(order, localDateTime);
        orderMapper.addOrderDetails(order);
    }

    @Override
    public List<Order> listOrder(Integer orderId) {
        return orderMapper.listOrder(orderId);
    }

    @Override
    public List<OrderInfoAddDate> listOrders(Integer uid) {
        return orderMapper.listOrders(uid);
    }

    @Override
    public List<Order> getOrderDetails(Integer orderId) {
        return orderMapper.getOrderDetail(orderId);
    }

    @Override
    public Double getTotalPrice(Integer uid) {
        return orderMapper.getTotalPrice(uid);
    }

    @Override
    public List<Integer> getOrderNumber(Integer uid, boolean status) {
        return orderMapper.getOrderNumber(uid, status);
    }

    @Transactional
    @Override
    public void updateOrder(OrderRequest order) {
        int retryCount = 0;
        long delay = RETRY_DELAY;
        while (true) {
            try {
                if (orderMapper.getOrderDetailsNumber(order) > 0) {
                    orderMapper.deleteOrderDetails(order);
                }
                orderMapper.updateOrderTotalPrice(order);
                if (order.getShoppingCart() != null && !order.getShoppingCart().isEmpty()) {
                    orderMapper.addOrderDetails(order);
                }
                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    throw new RuntimeException("Transaction failed after multiple retries", e);
                }
                try {
                    delay *= 2;
                    TimeUnit.MILLISECONDS.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void deleteOrder(List<Integer> orderId) {
        orderMapper.deleteOrder(orderId);
    }

    @Transactional
    @Override
    public Map<String, String> payOrder(OrderInfo order) {
        Integer uid = order.getUid();
        Integer orderId = order.getOrderId();
        LocalDateTime localDateTime = LocalDateTime.now();
        Double balance = userMapper.getBalance(uid);
        Double totalPrice = getTotalPrice(uid);
        if (totalPrice <= EXEMPT_CARRIAGE) {
            totalPrice += CARRIAGE_PRICE;
        }

        BigDecimal balanceBD = BigDecimal.valueOf(balance);
        BigDecimal totalPriceBD = BigDecimal.valueOf(totalPrice);

        if (balanceBD.compareTo(totalPriceBD) < 0) {
            throw new RuntimeException("余额不足");
        }

        BigDecimal newBalance = balanceBD.subtract(totalPriceBD).setScale(2, RoundingMode.HALF_UP);
        userMapper.updateBalance(uid, newBalance.doubleValue());

        String pickupCode = generatePickupCode(LocalDate.now());
        String payload = orderId + "|" + pickupCode + "|" + localDateTime;
        String pickupToken = pickupTokenUtil.encrypt(payload);

        order.setPickupCode(pickupCode);
        order.setPickupToken(pickupToken);
        order.setPickupStatus(0);
        order.setPickupTime(null);

        orderMapper.setOrderStatus(order, localDateTime);

        List<Map<String, Object>> productInfo = orderMapper.getOrderDetailsProductCodes(orderId);
        log.info("订单{}文创产品信息：{}", orderId, productInfo);
        for (Map<String, Object> entry : productInfo) {
            Integer quantity = (Integer) entry.get("purchaseNum");
            String productCode = (String) entry.get("productCode");
            productMapper.updateProductStock(quantity, productCode);
        }
        Map<String, String> pickupInfo = new HashMap<>();
        pickupInfo.put("pickupCode", pickupCode);
        pickupInfo.put("pickupToken", pickupToken);
        return pickupInfo;
    }

    @Override
    public String verifyPickup(String token) {
        String payload = pickupTokenUtil.decrypt(token);
        String[] parts = payload.split("\\|");
        if (parts.length < 2) {
            throw new RuntimeException("取件码无效");
        }
        Integer orderId = Integer.parseInt(parts[0]);
        String pickupCode = parts[1];
        Map<String, Object> pickupInfo = orderMapper.getPickupInfo(orderId);
        if (pickupInfo == null || pickupInfo.isEmpty()) {
            throw new RuntimeException("订单不存在");
        }
        String storedToken = pickupInfo.get("pickupToken") == null ? "" : pickupInfo.get("pickupToken").toString();
        Integer pickupStatus = pickupInfo.get("pickupStatus") == null ? 0 : Integer.parseInt(pickupInfo.get("pickupStatus").toString());
        String storedCode = pickupInfo.get("pickupCode") == null ? "" : pickupInfo.get("pickupCode").toString();

        if (!storedToken.equals(token) || !storedCode.equals(pickupCode)) {
            throw new RuntimeException("取件码不匹配");
        }
        if (pickupStatus == 1) {
            return "已取货";
        }
        orderMapper.markPickup(orderId, LocalDateTime.now());
        return "已取货";
    }

    private String generatePickupCode(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        int count = orderMapper.countOrdersByDate(start, end);
        String seq = String.format("%04d", count + 1);
        return randomLetters(3) + "-" + seq;
    }

    private String randomLetters(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(LETTERS.length());
            builder.append(LETTERS.charAt(idx));
        }
        return builder.toString();
    }
}
