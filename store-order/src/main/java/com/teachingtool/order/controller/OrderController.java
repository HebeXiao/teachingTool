package com.teachingtool.order.controller;

import com.teachingtool.order.service.OrderService;
import com.teachingtool.param.OrderParam;
import com.teachingtool.param.PageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {


    @Autowired
    private OrderService orderService;

    /**
     * 订单数据保存
     * @param orderParam
     * @return
     */
    @PostMapping("save")
    public Object save(@RequestBody OrderParam orderParam){


        return orderService.save(orderParam);
    }


    /**
     * 订单集合查询,注意,按照类别查询!
     * @param orderParam
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody OrderParam orderParam){

        return orderService.list(orderParam);
    }


    /**
     * 检查订单是否包含要删除的商品
     * @param productId
     * @return
     */
    @PostMapping("/check")
    public  Object check(@RequestBody Integer productId){
        return orderService.check(productId);
    }

    @PostMapping("/detail")
    public Map<String, Object> orderDetail(@RequestBody Map<String, Long> param){
        return orderService.getOrderDetail(param.get("order_id"));
    }

    @GetMapping("/order/detail")
    public Map<String, Object> orderDetail(@RequestParam("order_id") Long orderId) {
        return orderService.getOrderDetail(orderId);
    }
}
