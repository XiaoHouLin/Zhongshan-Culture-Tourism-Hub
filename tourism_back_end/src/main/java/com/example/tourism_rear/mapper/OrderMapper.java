package com.example.tourism_rear.mapper;

import com.example.tourism_rear.pack.utils.Order;
import com.example.tourism_rear.pack.utils.OrderInfo;
import com.example.tourism_rear.pack.utils.OrderInfoAddDate;
import com.example.tourism_rear.pack.utils.OrderRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    // 添加订单
    @Insert("INSERT INTO `order`(uid, CreateDate, totalPrice, isCompleted) VALUES (#{order.uid},#{localDateTime},#{order.totalProductPrice},false)")
    @Options(useGeneratedKeys = true, keyProperty = "order.orderId", keyColumn = "orderId")
    void addOrder(OrderRequest order, LocalDateTime localDateTime);

    // 添加订单详情
    void addOrderDetails(OrderRequest order);

    // 查询订单
    @Select("SELECT productCode, purchaseNum, price, productName FROM orderDetails WHERE orderId = #{orderId}")
    List<Order> listOrder(Integer orderId);

    @Select("SELECT * FROM `order` WHERE uid = #{uid}")
    List<OrderInfoAddDate> listOrders(Integer uid);

    // 查询订单总价
    @Select("SELECT totalPrice FROM `order` WHERE uid = #{uid} and isCompleted = 0")
    Double getTotalPrice(Integer uid);

    @Select("SELECT count(*) FROM `order` WHERE CreateDate >= #{start} AND CreateDate < #{end}")
    int countOrdersByDate(LocalDateTime start, LocalDateTime end);

    // 根据uid查询订单数量
    List<Integer> getOrderNumber(Integer uid, boolean status);

    // 删除订单详情
    @Select("DELETE FROM orderDetails WHERE orderId = #{orderId}")
    void deleteOrderDetails(OrderRequest order);

    // 更新订单总价
    @Select("UPDATE `order` SET totalPrice = #{totalProductPrice} WHERE orderId = #{orderId}")
    void updateOrderTotalPrice(OrderRequest order);

    // 获取订单详情中购买的文创产品数量
    @Select("SELECT count(*) FROM orderDetails WHERE orderId = #{orderId}")
    int getOrderDetailsNumber(OrderRequest order);

    // 根据orderId获取订单详情中所有的文创产品编号以及对应购买数量
    @Select("SELECT productCode, purchaseNum FROM orderDetails WHERE orderId = #{orderId}")
    List<Map<String, Object>> getOrderDetailsProductCodes(Integer orderId);

    // 获取订单详情
    @Select("SELECT productCode, purchaseNum, price, productName FROM orderDetails WHERE orderId = #{orderId}")
    List<Order> getOrderDetail(Integer orderId);

    @Select("SELECT pickupToken, pickupStatus, pickupCode FROM `order` WHERE orderId = #{orderId}")
    Map<String, Object> getPickupInfo(Integer orderId);

    @Update("UPDATE `order` SET pickupStatus = 1, pickupTime = #{pickupTime} WHERE orderId = #{orderId}")
    void markPickup(Integer orderId, LocalDateTime pickupTime);

    // 删除订单
    void deleteOrder(List<Integer> orderId);

    // 支付订单，设置订单状态，更新用户余额
    void setOrderStatus(OrderInfo order, LocalDateTime localDateTime);
}
