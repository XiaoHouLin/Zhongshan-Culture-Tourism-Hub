package com.example.tourism_rear.service.impl;

import com.example.tourism_rear.mapper.CulturalProductMapper;
import com.example.tourism_rear.mapper.OrderMapper;
import com.example.tourism_rear.mapper.UserMapper;
import com.example.tourism_rear.pack.utils.Order;
import com.example.tourism_rear.pack.utils.OrderInfo;
import com.example.tourism_rear.pack.utils.OrderInfoAddDate;
import com.example.tourism_rear.pack.utils.OrderRequest;
import com.example.tourism_rear.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 1000; // 1秒
    private static final int EXEMPT_CARRIAGE = 60;
    private static final int CARRIAGE_PRICE = 10;


    /**
     * 添加订单/购物车,添加订单详情
     *
     * @author : 🐒
     */
    @Transactional
    @Override
    public void addOrder(OrderRequest order, LocalDateTime localDateTime) {
        orderMapper.addOrder(order, localDateTime);
        orderMapper.addOrderDetails(order);
    }

    /**
     * 根据orderId查询订单列表
     *
     * @author : 🐒
     */
    @Override
    public List<Order> listOrder(Integer orderId) {
        return orderMapper.listOrder(orderId);
    }

    /**
     * 根据uid查询订单列表
     *
     * @author : 🐒
     */
    @Override
    public List<OrderInfoAddDate> listOrders(Integer uid) {
        return orderMapper.listOrders(uid);
    }

    /**
     * 根据orderId查询订单详情
     *
     * @author : 🐒
     */
    @Override
    public List<Order> getOrderDetails(Integer orderId) {
        return orderMapper.getOrderDetail(orderId);
    }

    /**
     * 查询订单总价
     *
     * @author : 🐒
     */
    @Override
    public Double getTotalPrice(Integer uid) {
        return orderMapper.getTotalPrice(uid);
    }

    /**
     * 查询订单数,当status为true时查询已完成订单，为false时查询未完成订单
     *
     * @author : 🐒
     */
    @Override
    public List<Integer> getOrderNumber(Integer uid, boolean status) {
        return orderMapper.getOrderNumber(uid, status);
    }

    /**
     * 修改订单状态,删除旧数据插入新数据，更新总价
     *
     * @author : 🐒
     */
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
                return; // 成功执行，退出循环
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

    /**
     * 删除订单
     *
     * @author : 🐒
     */
    @Override
    public void deleteOrder(List<Integer> orderId) {
        orderMapper.deleteOrder(orderId);
    }

    /**
     * 支付订单;检查余额，更新用户余额，设置订单状态（已完成）
     *
     * @author : 🐒
     */
    @Transactional
    @Override
    public void payOrder(OrderInfo order) {
        Integer uid = order.getUid();
        Integer orderId = order.getOrderId();
        LocalDateTime localDateTime = LocalDateTime.now();
        Double balance = userMapper.getBalance(uid);
        Double totalPrice = getTotalPrice(uid);
        if (totalPrice <= EXEMPT_CARRIAGE) {
            totalPrice += CARRIAGE_PRICE;
        }
        // 将 double 转换为 BigDecimal
        BigDecimal balanceBD = BigDecimal.valueOf(balance);
        BigDecimal totalPriceBD = BigDecimal.valueOf(totalPrice);

        if (balanceBD.compareTo(totalPriceBD) < 0) {
            throw new RuntimeException("余额不足");
        }

        // 计算新的余额并保留两位小数
        BigDecimal newBalance = balanceBD.subtract(totalPriceBD).setScale(2, RoundingMode.HALF_UP);

        // 更新用户余额
        userMapper.updateBalance(uid, newBalance.doubleValue());

        // 更新订单状态
        orderMapper.setOrderStatus(order, localDateTime);

        // 获取订单中文创产品编号，然后更新库存
        List<Map<String, Object>> productInfo = orderMapper.getOrderDetailsProductCodes(orderId);
        log.info("订单{}文创产品信息：{}", orderId, productInfo);
        for (Map<String, Object> entry : productInfo) {
            Integer quantity = (Integer) entry.get("purchaseNum");
            String productCode = (String) entry.get("productCode");
            productMapper.updateProductStock(quantity, productCode);
        }
    }
}
