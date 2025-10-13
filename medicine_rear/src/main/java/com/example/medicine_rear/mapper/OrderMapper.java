package com.example.medicine_rear.mapper;

import com.example.medicine_rear.pack.utils.Order;
import com.example.medicine_rear.pack.utils.OrderInfo;
import com.example.medicine_rear.pack.utils.OrderInfoAddDate;
import com.example.medicine_rear.pack.utils.OrderRequest;
import org.apache.ibatis.annotations.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    //添加订单
    @Insert("INSERT INTO `order`(uid, CreateDate, totalPrice,isCompleted) VALUES (#{order.uid},#{localDateTime},#{order.allDrugPrice},false)")
    @Options(useGeneratedKeys = true, keyProperty = "order.orderId", keyColumn = "orderId")
    void addOrder(OrderRequest order, LocalDateTime localDateTime);

    //添加订单详情
    void addOrderDetails(OrderRequest order);

    //查询订单
    @Select("SELECT mno, purchaseNum, price, mname FROM orderDetails WHERE orderId = #{orderId}")
    List<Order> listOrder(Integer orderId);

    @Select("SELECT * FROM `order` WHERE uid = #{uid}")
    List<OrderInfoAddDate> listOrders(Integer uid);

    //查询订单总价
    @Select("SELECT totalPrice FROM `order` WHERE uid = #{uid} and isCompleted = 0")
    Double getTotalPrice(Integer uid);

    //根据uid查询未完成订单数量
    List<Integer> getOrderNumber(Integer uid, boolean status);

    //删除订单详情
    @Select("DELETE FROM orderDetails WHERE orderId = #{orderId}")
    void deleteOrderDetails(OrderRequest order);

    //更新订单总价
    @Select("UPDATE `order` SET totalPrice = #{allDrugPrice} WHERE orderId = #{orderId}")
    void updateOrderTotalPrice(OrderRequest order);

    //获取订单详情购买的药物种类(条数)
    @Select("SELECT count(*) FROM orderDetails WHERE orderId = #{orderId}")
    int getOrderDetailsNumber(OrderRequest order);

    //根据orderId获取订单详情中所有的药物编号mno,以及对应购买数量
    @Select("SELECT mno,purchaseNum FROM orderDetails WHERE orderId = #{orderId}")
    @MapKey("mno")
    List<Map<String, Integer>> getOrderDetailsMno(Integer orderId);

    //获取订单详情
    @Select("SELECT mno, purchaseNum, price, mname FROM orderDetails WHERE orderId = #{orderId}")
    List<Order> getOrderDetail(Integer orderId);

    //删除订单
    void deleteOrder(List<Integer> orderId);

    //支付订单，设置订单状态，更新用户余额
    void setOrderStatus(OrderInfo order, LocalDateTime localDateTime);

}
