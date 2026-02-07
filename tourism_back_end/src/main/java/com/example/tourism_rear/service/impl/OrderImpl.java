package com.example.tourism_rear.service.impl;

import com.example.tourism_rear.mapper.CulturalProductMapper;
import com.example.tourism_rear.mapper.OrderMapper;
import com.example.tourism_rear.mapper.UserMapper;
import com.example.tourism_rear.pack.utils.CulturalProductInfo;
import com.example.tourism_rear.pack.utils.Order;
import com.example.tourism_rear.pack.utils.OrderAdminView;
import com.example.tourism_rear.pack.utils.OrderInfo;
import com.example.tourism_rear.pack.utils.OrderInfoAddDate;
import com.example.tourism_rear.pack.utils.OrderRequest;
import com.example.tourism_rear.pack.utils.OrderStatusUpdate;
import com.example.tourism_rear.pack.utils.PickupTokenUtil;
import com.example.tourism_rear.service.MessageService;
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
    @Autowired
    private MessageService messageService;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 1000;
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final long PAYMENT_EXPIRE_MINUTES = 15;
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
        closeExpiredOrders();
        return orderMapper.listOrders(uid);
    }

    @Override
    public OrderInfoAddDate getActiveOrderByUid(Integer uid) {
        closeExpiredOrders();
        return orderMapper.getActiveOrderByUid(uid);
    }

    @Override
    public List<Order> getOrderDetails(Integer orderId) {
        return orderMapper.getOrderDetail(orderId);
    }

    @Override
    public Double getTotalPrice(Integer uid) {
        closeExpiredOrders();
        return orderMapper.getTotalPrice(uid);
    }

    @Override
    public List<Integer> getOrderNumber(Integer uid, boolean status) {
        closeExpiredOrders();
        return orderMapper.getOrderNumber(uid, status);
    }

    @Transactional
    @Override
    public void updateOrder(OrderRequest order) {
        ensureOrderActive(order == null ? null : order.getOrderId());
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
        if (orderId == null) {
            throw new RuntimeException("订单信息缺失");
        }
        OrderInfoAddDate existing = orderMapper.getOrderInfoById(orderId);
        if (existing == null) {
            throw new RuntimeException("订单不存在");
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Integer status = existing.getOrderStatus() == null ? 0 : existing.getOrderStatus();
        if (status == 0 && isExpired(existing, localDateTime)) {
            closeExpiredOrders();
            throw new RuntimeException("订单已过期，请重新下单");
        }
        if (status != 0 && status != 4) {
            throw new RuntimeException("订单状态异常，请重新下单");
        }
        validatePurchaseRules(uid, orderId);
        Double balance = userMapper.getBalance(uid);
        Double totalPrice = existing.getTotalPrice() == null ? getTotalPrice(uid) : existing.getTotalPrice();
        if (totalPrice == null) {
            throw new RuntimeException("订单金额异常");
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
        messageService.createMessageIfAbsent(
                uid,
                "ORDER_PICKUP",
                String.valueOf(orderId),
                "订单待取货",
                "订单 " + orderId + " 已支付，请查看取件码与取货信息。"
        );
        Map<String, String> pickupInfo = new HashMap<>();
        pickupInfo.put("pickupCode", pickupCode);
        pickupInfo.put("pickupToken", pickupToken);
        return pickupInfo;
    }

    @Override
    public Map<String, Object> getPickupInfo(String token) {
        PickupPayload payload = parsePickupToken(token);
        Map<String, Object> pickupInfo = validatePickupToken(payload, token);
        OrderInfoAddDate orderInfo = orderMapper.getOrderInfoById(payload.orderId());
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }
        List<Order> details = orderMapper.getOrderDetail(payload.orderId());
        Map<String, Object> result = new HashMap<>();
        result.put("orderInfo", orderInfo);
        result.put("orderDetails", details);
        result.put("pickupStatus", pickupInfo.getOrDefault("pickupStatus", 0));
        return result;
    }

    @Override
    public String confirmPickup(String token) {
        PickupPayload payload = parsePickupToken(token);
        Map<String, Object> pickupInfo = validatePickupToken(payload, token);
        Integer pickupStatus = pickupInfo.get("pickupStatus") == null ? 0 : Integer.parseInt(pickupInfo.get("pickupStatus").toString());
        if (pickupStatus == 1) {
            return "已取货";
        }
        orderMapper.markPickup(payload.orderId(), LocalDateTime.now());
        return "已确认取货";
    }

    @Override
    public List<OrderAdminView> listAllOrders() {
        closeExpiredOrders();
        return orderMapper.listAllOrders();
    }

    @Override
    public void updateOrderStatus(OrderStatusUpdate update) {
        if (update == null || update.getOrderId() == null) {
            throw new RuntimeException("订单信息缺失");
        }
        int status = update.getOrderStatus() == null ? 0 : update.getOrderStatus();
        if (status < 0 || status > 4) {
            throw new RuntimeException("订单状态非法");
        }
        OrderInfoAddDate orderInfo = orderMapper.getOrderInfoById(update.getOrderId());
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }
        int previousStatus = orderInfo.getOrderStatus() == null ? 0 : orderInfo.getOrderStatus();
        if (previousStatus == 4 && status != 4) {
            throw new RuntimeException("已退款订单不可变更，需用户重新支付");
        }
        LocalDateTime now = LocalDateTime.now();
        if (status == 4) {
            if (previousStatus == 4) {
                return;
            }
            if (previousStatus == 0 || previousStatus == 2) {
                throw new RuntimeException("订单未支付，无法退款");
            }
            if (previousStatus != 4) {
                Double total = orderInfo.getTotalPrice() == null ? 0.0 : orderInfo.getTotalPrice();
                if (total > 0) {
                    Double balance = userMapper.getBalance(orderInfo.getUid());
                    BigDecimal newBalance = BigDecimal.valueOf(balance).add(BigDecimal.valueOf(total))
                            .setScale(2, RoundingMode.HALF_UP);
                    userMapper.updateBalance(orderInfo.getUid(), newBalance.doubleValue());
                }
            }
            List<Map<String, Object>> productInfo = orderMapper.getOrderDetailsProductCodes(update.getOrderId());
            if (productInfo != null) {
                for (Map<String, Object> entry : productInfo) {
                    Integer quantity = entry.get("purchaseNum") == null ? 0 : Integer.parseInt(entry.get("purchaseNum").toString());
                    String productCode = entry.get("productCode") == null ? null : entry.get("productCode").toString();
                    if (productCode != null && quantity != null && quantity > 0) {
                        productMapper.restoreProductStock(quantity, productCode);
                    }
                }
            }
            orderMapper.updateOrderStatus(update.getOrderId(), 4, 1, 0, null, now);
            messageService.createMessageIfAbsent(
                    orderInfo.getUid(),
                    "ORDER_REFUND",
                    String.valueOf(update.getOrderId()),
                    "订单已退款",
                    "订单 " + update.getOrderId() + " 已退款，可进入订单详情重新支付。"
            );
            return;
        }
        Integer isCompleted = status == 0 ? 0 : 1;
        Integer pickupStatus = status == 3 ? 1 : 0;
        LocalDateTime pickupTime = status == 3 ? now : null;
        LocalDateTime completeDate = status == 0 ? null : now;
        orderMapper.updateOrderStatus(update.getOrderId(), status, isCompleted, pickupStatus, pickupTime, completeDate);
        if (status == 1) {
            if (orderInfo != null && (orderInfo.getPickupCode() == null || orderInfo.getPickupToken() == null)) {
                String pickupCode = generatePickupCode(LocalDate.now());
                String payload = update.getOrderId() + "|" + pickupCode + "|" + now;
                String pickupToken = pickupTokenUtil.encrypt(payload);
                orderMapper.updatePickupInfo(update.getOrderId(), pickupCode, pickupToken);
            }
            messageService.createMessageIfAbsent(
                    orderInfo.getUid(),
                    "ORDER_PICKUP",
                    String.valueOf(update.getOrderId()),
                    "订单待取货",
                    "订单 " + update.getOrderId() + " 待取货，请查看取件码与取货信息。"
            );
        }
    }

    @Override
    public void closeOrder(Integer orderId, Integer uid, boolean isAdmin) {
        if (orderId == null) {
            throw new RuntimeException("订单信息缺失");
        }
        if (!isAdmin) {
            Integer owner = orderMapper.getOrderUid(orderId);
            if (owner == null || !owner.equals(uid)) {
                throw new RuntimeException("无权限");
            }
        }
        OrderStatusUpdate update = new OrderStatusUpdate();
        update.setOrderId(orderId);
        update.setOrderStatus(2);
        updateOrderStatus(update);
    }

    @Override
    public void deleteOrders(List<Integer> orderIds, Integer uid, boolean isAdmin) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new RuntimeException("订单信息缺失");
        }
        if (!isAdmin) {
            List<Integer> owners = orderMapper.getOrderUids(orderIds);
            if (owners.size() != orderIds.size()) {
                throw new RuntimeException("订单不存在");
            }
            if (owners.stream().anyMatch(owner -> owner == null || !owner.equals(uid))) {
                throw new RuntimeException("无权限");
            }
        }
        orderMapper.deleteOrderDetailsByOrderIds(orderIds);
        orderMapper.deleteOrder(orderIds);
    }

    @Override
    public int closeExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireBefore = now.minusMinutes(PAYMENT_EXPIRE_MINUTES);
        return orderMapper.closeExpiredOrders(expireBefore, now);
    }

    @Override
    public int getMonthlyProductPurchaseSum(Integer uid, String productCode) {
        if (uid == null || productCode == null || productCode.isBlank()) {
            return 0;
        }
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        return orderMapper.getMonthlyProductPurchaseSum(uid, productCode, startOfMonth, endOfMonth);
    }

    @Override
    public void createPendingPaymentMessage(Integer uid, Integer orderId) {
        if (uid == null || orderId == null) {
            throw new RuntimeException("订单信息缺失");
        }
        OrderInfoAddDate orderInfo = orderMapper.getOrderInfoById(orderId);
        if (orderInfo == null || orderInfo.getUid() == null || !orderInfo.getUid().equals(uid)) {
            throw new RuntimeException("订单不存在");
        }
        Integer status = orderInfo.getOrderStatus() == null ? 0 : orderInfo.getOrderStatus();
        if (status != 0) {
            return;
        }
        messageService.createMessageIfAbsent(
                uid,
                "ORDER_PENDING",
                String.valueOf(orderId),
                "订单待支付",
                "订单 " + orderId + " 尚未支付，请及时完成付款。"
        );
    }

    private void ensureOrderActive(Integer orderId) {
        if (orderId == null) {
            throw new RuntimeException("订单信息缺失");
        }
        OrderInfoAddDate orderInfo = orderMapper.getOrderInfoById(orderId);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (isExpired(orderInfo, now)) {
            closeExpiredOrders();
            throw new RuntimeException("订单已过期，请重新下单");
        }
        Integer status = orderInfo.getOrderStatus() == null ? 0 : orderInfo.getOrderStatus();
        if (status != 0) {
            throw new RuntimeException("订单已关闭或已支付，请重新下单");
        }
    }

    private boolean isExpired(OrderInfoAddDate orderInfo, LocalDateTime now) {
        if (orderInfo == null || orderInfo.getCreateDate() == null) {
            return false;
        }
        return orderInfo.getCreateDate().plusMinutes(PAYMENT_EXPIRE_MINUTES).isBefore(now);
    }

    private PickupPayload parsePickupToken(String token) {
        String payload = pickupTokenUtil.decrypt(token);
        String[] parts = payload.split("\\|");
        if (parts.length < 2) {
            throw new RuntimeException("取件码无效");
        }
        Integer orderId = Integer.parseInt(parts[0]);
        String pickupCode = parts[1];
        return new PickupPayload(orderId, pickupCode);
    }

    private Map<String, Object> validatePickupToken(PickupPayload payload, String token) {
        Map<String, Object> pickupInfo = orderMapper.getPickupInfo(payload.orderId());
        if (pickupInfo == null || pickupInfo.isEmpty()) {
            throw new RuntimeException("订单不存在");
        }
        String storedToken = pickupInfo.get("pickupToken") == null ? "" : pickupInfo.get("pickupToken").toString();
        String storedCode = pickupInfo.get("pickupCode") == null ? "" : pickupInfo.get("pickupCode").toString();
        if (!storedToken.equals(token) || !storedCode.equals(payload.pickupCode())) {
            throw new RuntimeException("取件码不匹配");
        }
        return pickupInfo;
    }

    private record PickupPayload(Integer orderId, String pickupCode) {}

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

    private void validatePurchaseRules(Integer uid, Integer orderId) {
        if (uid == null || orderId == null) {
            throw new RuntimeException("订单信息缺失");
        }
        List<Map<String, Object>> productInfo = orderMapper.getOrderDetailsProductCodes(orderId);
        if (productInfo == null || productInfo.isEmpty()) {
            throw new RuntimeException("订单详情为空");
        }
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        for (Map<String, Object> entry : productInfo) {
            String productCode = entry.get("productCode") == null ? null : entry.get("productCode").toString();
            Integer quantity = entry.get("purchaseNum") == null ? 0 : Integer.parseInt(entry.get("purchaseNum").toString());
            if (productCode == null) {
                continue;
            }
            CulturalProductInfo product = productMapper.getProductByCode(productCode);
            if (product != null && product.getDisplayMode() != null && product.getDisplayMode().contains("线下")) {
                throw new RuntimeException("门店限定商品无法在线购买");
            }
            int purchased = orderMapper.getMonthlyProductPurchaseSum(uid, productCode, startOfMonth, endOfMonth);
            if (purchased + quantity > 5) {
                throw new RuntimeException("每月每件商品限购5件");
            }
        }
    }
}
