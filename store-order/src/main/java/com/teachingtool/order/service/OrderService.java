package com.teachingtool.order.service;

import com.teachingtool.param.OrderParam;
import com.teachingtool.utils.R;

import java.util.Map;

public interface OrderService {
    Object save(OrderParam orderParam);

    Object check(Integer productId);

    Map<String, Object> getOrderDetail(Long orderId);

    R list(Integer userId, String token);
}
