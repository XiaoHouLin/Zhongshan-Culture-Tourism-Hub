package com.example.tourism_rear.mapper;

import com.example.tourism_rear.pack.utils.Order;
import com.example.tourism_rear.pack.utils.OrderInfo;
import com.example.tourism_rear.pack.utils.OrderInfoAddDate;
import com.example.tourism_rear.pack.utils.OrderRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    // 添加订单
    @Insert("INSERT INTO `order`(uid, CreateDate, totalPrice, isCompleted, orderStatus) VALUES (#{order.uid},#{localDateTime},#{order.totalProductPrice},false,0)")
    @Options(useGeneratedKeys = true, keyProperty = "order.orderId", keyColumn = "orderId")
    void addOrder(OrderRequest order, LocalDateTime localDateTime);

    // 添加订单详情
    void addOrderDetails(OrderRequest order);

    // 查询订单
    @Select("SELECT productCode, purchaseNum, price, productName FROM orderDetails WHERE orderId = #{orderId}")
    List<Order> listOrder(Integer orderId);

    @Select("SELECT * FROM `order` WHERE uid = #{uid}")
    List<OrderInfoAddDate> listOrders(Integer uid);

    @Select("SELECT * FROM `order` WHERE orderId = #{orderId}")
    OrderInfoAddDate getOrderInfoById(Integer orderId);

    @Select("SELECT * FROM `order` WHERE uid = #{uid} AND isCompleted = 0 AND orderStatus = 0 ORDER BY CreateDate DESC LIMIT 1")
    OrderInfoAddDate getActiveOrderByUid(Integer uid);

    @Select("SELECT uid FROM `order` WHERE orderId = #{orderId}")
    Integer getOrderUid(Integer orderId);

    @Select("SELECT o.orderId, o.uid, u.username, u.authority, COALESCE(o.phone, c.cphone) AS phone, o.email, o.name, o.address, o.remark, " +
            "o.totalPrice, o.CreateDate, o.CompleteDate, o.pickupCode, o.pickupStatus, o.pickupTime, o.orderStatus, " +
            "o.refundReason, o.refundOriginStatus, o.refundRequestTime " +
            "FROM `order` o " +
            "LEFT JOIN user u ON o.uid = u.uid " +
            "LEFT JOIN client c ON o.uid = c.uid " +
            "ORDER BY o.CreateDate DESC")
    List<com.example.tourism_rear.pack.utils.OrderAdminView> listAllOrders();

    // 查询订单总价
    @Select("SELECT totalPrice FROM `order` WHERE uid = #{uid} and isCompleted = 0")
    Double getTotalPrice(Integer uid);

    @Update("UPDATE `order` SET orderStatus = 2, isCompleted = 1, pickupStatus = 0, pickupTime = NULL, CompleteDate = #{closeTime} " +
            "WHERE orderStatus = 0 AND isCompleted = 0 AND CreateDate < #{expireBefore}")
    int closeExpiredOrders(@Param("expireBefore") LocalDateTime expireBefore, @Param("closeTime") LocalDateTime closeTime);

    @Select("SELECT count(*) FROM `order` WHERE CreateDate >= #{start} AND CreateDate < #{end}")
    int countOrdersByDate(LocalDateTime start, LocalDateTime end);

    // 根据 uid 查询订单数量
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

    // 根据 orderId 获取订单详情中所有文创产品编号及对应购买数量
    @Select("SELECT productCode, purchaseNum FROM orderDetails WHERE orderId = #{orderId}")
    List<Map<String, Object>> getOrderDetailsProductCodes(Integer orderId);

    // 获取订单详情
    @Select("SELECT productCode, purchaseNum, price, productName FROM orderDetails WHERE orderId = #{orderId}")
    List<Order> getOrderDetail(Integer orderId);

    @Select("SELECT pickupToken, pickupStatus, pickupCode FROM `order` WHERE orderId = #{orderId}")
    Map<String, Object> getPickupInfo(Integer orderId);

    @Delete("DELETE od FROM orderDetails od JOIN `order` o ON od.orderId = o.orderId WHERE o.uid = #{uid}")
    void deleteOrderDetailsByUid(Integer uid);

    @Delete("DELETE FROM `order` WHERE uid = #{uid}")
    void deleteOrdersByUid(Integer uid);

    @Update("UPDATE `order` SET pickupStatus = 1, pickupTime = #{pickupTime}, isCompleted = 1, CompleteDate = #{pickupTime}, orderStatus = 3 WHERE orderId = #{orderId}")
    void markPickup(Integer orderId, LocalDateTime pickupTime);

    List<Integer> getOrderUids(@org.apache.ibatis.annotations.Param("orderIds") List<Integer> orderIds);

    void deleteOrderDetailsByOrderIds(@org.apache.ibatis.annotations.Param("orderIds") List<Integer> orderIds);

    @Update("UPDATE `order` SET orderStatus = #{orderStatus}, isCompleted = #{isCompleted}, pickupStatus = #{pickupStatus}, pickupTime = #{pickupTime}, CompleteDate = #{completeDate} WHERE orderId = #{orderId}")
    void updateOrderStatus(Integer orderId, Integer orderStatus, Integer isCompleted, Integer pickupStatus, LocalDateTime pickupTime, LocalDateTime completeDate);

    @Update("UPDATE `order` SET orderStatus = #{orderStatus}, isCompleted = #{isCompleted}, pickupStatus = #{pickupStatus}, " +
            "pickupTime = #{pickupTime}, CompleteDate = #{completeDate}, CreateDate = #{createDate} WHERE orderId = #{orderId}")
    void updateOrderStatusWithCreateDate(Integer orderId, Integer orderStatus, Integer isCompleted, Integer pickupStatus,
                                         LocalDateTime pickupTime, LocalDateTime completeDate, LocalDateTime createDate);

    @Update("UPDATE `order` SET pickupCode = #{pickupCode}, pickupToken = #{pickupToken} WHERE orderId = #{orderId}")
    void updatePickupInfo(@Param("orderId") Integer orderId, @Param("pickupCode") String pickupCode, @Param("pickupToken") String pickupToken);

    @Select({
            "<script>",
            "SELECT COUNT(DISTINCT o.uid)",
            "FROM `order` o",
            "JOIN orderDetails od ON o.orderId = od.orderId",
            "WHERE o.orderStatus = 0",
            "AND od.productCode IN",
            "<foreach collection='productCodes' item='code' open='(' separator=',' close=')'>",
            "#{code}",
            "</foreach>",
            "</script>"
    })
    int countPendingPayingUsers(@Param("productCodes") List<String> productCodes);

    @Select({
            "<script>",
            "SELECT DISTINCT o.orderId",
            "FROM `order` o",
            "JOIN orderDetails od ON o.orderId = od.orderId",
            "WHERE o.orderStatus = 1",
            "AND o.CreateDate &gt;= #{start}",
            "AND o.CreateDate &lt; #{end}",
            "AND od.productCode IN",
            "<foreach collection='productCodes' item='code' open='(' separator=',' close=')'>",
            "#{code}",
            "</foreach>",
            "</script>"
    })
    List<Integer> findPendingPickupOrdersByProducts(@Param("productCodes") List<String> productCodes,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    @Delete({
            "<script>",
            "DELETE FROM orderDetails",
            "WHERE orderId = #{orderId}",
            "AND productCode IN",
            "<foreach collection='productCodes' item='code' open='(' separator=',' close=')'>",
            "#{code}",
            "</foreach>",
            "</script>"
    })
    void deleteOrderDetailsByOrderAndProductCodes(@Param("orderId") Integer orderId,
                                                  @Param("productCodes") List<String> productCodes);

    @Select("SELECT COALESCE(SUM(od.purchaseNum), 0) " +
            "FROM orderDetails od JOIN `order` o ON od.orderId = o.orderId " +
            "WHERE o.uid = #{uid} AND od.productCode = #{productCode} " +
            "AND o.orderStatus IN (1,3,6) AND o.CreateDate >= #{start} AND o.CreateDate < #{end}")
    int getMonthlyProductPurchaseSum(@Param("uid") Integer uid,
                                     @Param("productCode") String productCode,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Update("UPDATE `order` SET orderStatus = 5, refundOriginStatus = #{originStatus}, refundReason = #{reason}, " +
            "refundRequestTime = #{requestTime} WHERE orderId = #{orderId}")
    void markRefundRequested(@Param("orderId") Integer orderId,
                             @Param("originStatus") Integer originStatus,
                             @Param("reason") String reason,
                             @Param("requestTime") LocalDateTime requestTime);

    @Update("UPDATE `order` SET refundOriginStatus = NULL, refundRequestTime = NULL WHERE orderId = #{orderId}")
    void clearRefundRequest(Integer orderId);

    // 删除订单
    void deleteOrder(List<Integer> orderId);

    // 支付订单，设置订单状态并更新用户余额
    void setOrderStatus(OrderInfo order, LocalDateTime localDateTime);
}
