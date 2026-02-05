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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    // 娣诲姞璁㈠崟
    @Insert("INSERT INTO `order`(uid, CreateDate, totalPrice, isCompleted, orderStatus) VALUES (#{order.uid},#{localDateTime},#{order.totalProductPrice},false,0)")
    @Options(useGeneratedKeys = true, keyProperty = "order.orderId", keyColumn = "orderId")
    void addOrder(OrderRequest order, LocalDateTime localDateTime);

    // 娣诲姞璁㈠崟璇︽儏
    void addOrderDetails(OrderRequest order);

    // 鏌ヨ璁㈠崟
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

    @Select("SELECT o.orderId, o.uid, u.username, COALESCE(o.phone, c.cphone) AS phone, o.email, o.name, o.address, o.remark, " +
            "o.CreateDate, o.CompleteDate, o.pickupCode, o.pickupStatus, o.pickupTime, o.orderStatus " +
            "FROM `order` o " +
            "LEFT JOIN user u ON o.uid = u.uid " +
            "LEFT JOIN client c ON o.uid = c.uid " +
            "ORDER BY o.CreateDate DESC")
    List<com.example.tourism_rear.pack.utils.OrderAdminView> listAllOrders();

    // 鏌ヨ璁㈠崟鎬讳环
    @Select("SELECT totalPrice FROM `order` WHERE uid = #{uid} and isCompleted = 0")
    Double getTotalPrice(Integer uid);

    @Update("UPDATE `order` SET orderStatus = 2, isCompleted = 1, pickupStatus = 0, pickupTime = NULL, CompleteDate = #{closeTime} " +
            "WHERE orderStatus = 0 AND isCompleted = 0 AND CreateDate < #{expireBefore}")
    int closeExpiredOrders(@Param("expireBefore") LocalDateTime expireBefore, @Param("closeTime") LocalDateTime closeTime);

    @Select("SELECT count(*) FROM `order` WHERE CreateDate >= #{start} AND CreateDate < #{end}")
    int countOrdersByDate(LocalDateTime start, LocalDateTime end);

    // 鏍规嵁uid鏌ヨ璁㈠崟鏁伴噺
    List<Integer> getOrderNumber(Integer uid, boolean status);

    // 鍒犻櫎璁㈠崟璇︽儏
    @Select("DELETE FROM orderDetails WHERE orderId = #{orderId}")
    void deleteOrderDetails(OrderRequest order);

    // 鏇存柊璁㈠崟鎬讳环
    @Select("UPDATE `order` SET totalPrice = #{totalProductPrice} WHERE orderId = #{orderId}")
    void updateOrderTotalPrice(OrderRequest order);

    // 鑾峰彇璁㈠崟璇︽儏涓喘涔扮殑鏂囧垱浜у搧鏁伴噺
    @Select("SELECT count(*) FROM orderDetails WHERE orderId = #{orderId}")
    int getOrderDetailsNumber(OrderRequest order);

    // 鏍规嵁orderId鑾峰彇璁㈠崟璇︽儏涓墍鏈夌殑鏂囧垱浜у搧缂栧彿浠ュ強瀵瑰簲璐拱鏁伴噺
    @Select("SELECT productCode, purchaseNum FROM orderDetails WHERE orderId = #{orderId}")
    List<Map<String, Object>> getOrderDetailsProductCodes(Integer orderId);

    // 鑾峰彇璁㈠崟璇︽儏
    @Select("SELECT productCode, purchaseNum, price, productName FROM orderDetails WHERE orderId = #{orderId}")
    List<Order> getOrderDetail(Integer orderId);

    @Select("SELECT pickupToken, pickupStatus, pickupCode FROM `order` WHERE orderId = #{orderId}")
    Map<String, Object> getPickupInfo(Integer orderId);

    @Update("UPDATE `order` SET pickupStatus = 1, pickupTime = #{pickupTime}, isCompleted = 1, CompleteDate = #{pickupTime}, orderStatus = 3 WHERE orderId = #{orderId}")
    void markPickup(Integer orderId, LocalDateTime pickupTime);

    List<Integer> getOrderUids(@org.apache.ibatis.annotations.Param("orderIds") List<Integer> orderIds);

    void deleteOrderDetailsByOrderIds(@org.apache.ibatis.annotations.Param("orderIds") List<Integer> orderIds);

    @Update("UPDATE `order` SET orderStatus = #{orderStatus}, isCompleted = #{isCompleted}, pickupStatus = #{pickupStatus}, pickupTime = #{pickupTime}, CompleteDate = #{completeDate} WHERE orderId = #{orderId}")
    void updateOrderStatus(Integer orderId, Integer orderStatus, Integer isCompleted, Integer pickupStatus, LocalDateTime pickupTime, LocalDateTime completeDate);

    @Update("UPDATE `order` SET pickupCode = #{pickupCode}, pickupToken = #{pickupToken} WHERE orderId = #{orderId}")
    void updatePickupInfo(@Param("orderId") Integer orderId, @Param("pickupCode") String pickupCode, @Param("pickupToken") String pickupToken);

    @Select("SELECT COALESCE(SUM(od.purchaseNum), 0) " +
            "FROM orderDetails od JOIN `order` o ON od.orderId = o.orderId " +
            "WHERE o.uid = #{uid} AND od.productCode = #{productCode} " +
            "AND o.orderStatus IN (1,3) AND o.CreateDate >= #{start} AND o.CreateDate < #{end}")
    int getMonthlyProductPurchaseSum(@Param("uid") Integer uid,
                                     @Param("productCode") String productCode,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    // 鍒犻櫎璁㈠崟
    void deleteOrder(List<Integer> orderId);

    // 鏀粯璁㈠崟锛岃缃鍗曠姸鎬侊紝鏇存柊鐢ㄦ埛浣欓
    void setOrderStatus(OrderInfo order, LocalDateTime localDateTime);
}
