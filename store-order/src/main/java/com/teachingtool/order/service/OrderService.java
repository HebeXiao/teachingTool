package com.teachingtool.order.service;

import com.teachingtool.param.OrderParam;
import com.teachingtool.utils.R;
import com.teachingtool.vo.OrderDetailVo;

import java.util.Map;

public interface OrderService {
    Object save(OrderParam orderParam);

    Object check(Integer productId);

    Map<String, Object> getOrderDetail(Long orderId);

    Map<String, Object> updateOrderDetail(Long orderId, OrderDetailVo updatedOrderDetail);

    R list(Integer userId, String token);
}
