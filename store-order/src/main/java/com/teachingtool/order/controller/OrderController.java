package com.teachingtool.order.controller;

import com.teachingtool.order.service.OrderService;
import com.teachingtool.param.OrderParam;
import com.teachingtool.param.PageParam;
import com.teachingtool.utils.R;
import com.teachingtool.vo.OrderDetailVo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.util.Map;
@Slf4j
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

    @GetMapping("/list")
    public R listOrdersByUserId(@RequestParam("user_id") Integer userId,
                                @RequestHeader(value = "Authorization", required = false, defaultValue = "") String token) {
        // 直接传递token和userId到Service层
        return orderService.list(userId, token);
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

    // 更新订单详细信息
    @PutMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> updateOrderDetail(
            @PathVariable Long orderId,
            @RequestBody OrderDetailVo updatedOrderDetail) {
        Map<String, Object> updatedDetail = orderService.updateOrderDetail(orderId, updatedOrderDetail);
        return ResponseEntity.ok(updatedDetail);
    }
}
