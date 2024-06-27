package com.teachingtool.order.service;

import com.teachingtool.param.OrderParam;

import java.util.Map;

public interface OrderService {
    Object save(OrderParam orderParam);

    Object list(OrderParam orderParam);

    Object check(Integer productId);

    Map<String, Object> getOrderDetail(Long orderId);
}
