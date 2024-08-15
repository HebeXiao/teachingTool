package com.teachingtool.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teachingtool.pojo.Order;
import com.teachingtool.vo.OrderDetailVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order> {
    List<Order> selectOrdersByOrderId(Long orderId);
    void updateOrderDetail(@Param("orderDetail") OrderDetailVo orderDetailVo);
}

