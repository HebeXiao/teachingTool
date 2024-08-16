package com.teachingtool.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachingtool.clients.CartClient;
import com.teachingtool.clients.ProductClient;
import com.teachingtool.clients.WebSocketClient;
import com.teachingtool.order.mapper.OrderMapper;
import com.teachingtool.order.service.OrderService;
import com.teachingtool.param.CartParam;
import com.teachingtool.param.OrderParam;
import com.teachingtool.param.ProductIdsParam;
import com.teachingtool.param.ProductNumberParam;
import com.teachingtool.pojo.Order;
import com.teachingtool.pojo.Product;
import com.teachingtool.service.CartService;
import com.teachingtool.utils.R;
import com.teachingtool.vo.CartVo;
import com.teachingtool.vo.OrderDetailVo;
import com.teachingtool.vo.OrderVo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl  extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final String SECRET_KEY = "your_secret_key";

    @Autowired
    private CartClient cartClient;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WebSocketClient webSocketClient;

    /**
     * Save orders
     * Use mq asynchronous for inventory and shopping cart to avoid distributed transactions!
     */
    @Transactional
    @Override
    public Object save(OrderParam orderParam) {
        List<Integer> cartIds = new ArrayList<>();
        List<Order>  orderList = new ArrayList<>();
        List<ProductNumberParam>  productNumberParamList  =
                new ArrayList<>();

        Integer userId = orderParam.getUserId();
        List<CartVo> products = orderParam.getProducts();
        long ctime = System.currentTimeMillis();

        for (CartVo cartVo : products) {
            cartIds.add(cartVo.getId());
            Order order = new Order();
            order.setOrderId(ctime);
            order.setUserId(userId);
            order.setOrderTime(ctime);
            order.setProductId(cartVo.getProductID());
            order.setProductNum(cartVo.getNum());
            order.setProductPrice(cartVo.getPrice());
            orderList.add(order);

            // Modify information storage
            ProductNumberParam productNumberParam = new ProductNumberParam();
            productNumberParam.setProductId(cartVo.getProductID());
            productNumberParam.setProductNum(cartVo.getNum());
            productNumberParamList.add(productNumberParam);
        }

        this.saveBatch(orderList);

        // After successfully saving the order, delete the items from the shopping cart
        for (CartVo cartVo : products) {
            CartParam cartParam = new CartParam();
            cartParam.setUserId(userId);
            cartParam.setProductId(cartVo.getProductID());
            cartClient.remove(cartParam);
        }

        R ok = R.ok("Order Generated Successfully!");
        return ok;
    }

    /**
     * Order data inquiry
     */
    public static final String CHALLENGE_SUCCESS_CODE = "999";

    @Override
    public R list(Integer userId, String token) {
        ObjectMapper mapper = new ObjectMapper();
        if (token == null || token.isEmpty()) {
            String messageJson = "";
            try {
                messageJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
                    put("userID", userId);
                    put("message", "Challenge failed: Triggered by empty token.");
                }});
                log.info("Sending WebSocket notification due to empty token.");
                webSocketClient.notifyClients(messageJson);
            } catch (JsonProcessingException e) {
                log.error("Error creating JSON message for empty token", e);
            }
            return R.fail("Token is empty.");
        } else if (!token.startsWith("Bearer ")) {
            return R.fail("Token is invalid");
        } else {
            token = token.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                Date expiration = claims.getExpiration();
                if (expiration.before(new Date())) {
                    return R.fail("Token has expired");
                }

                Integer tokenUserId = claims.get("userId", Integer.class);
                if (!userId.equals(tokenUserId)) {
                    String messageJson = "";
                    try {
                        messageJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
                            put("userID", userId);
                            put("message", "Challenge succeeded: Triggered by user ID mismatch.");
                        }});
                        log.info(messageJson);
                    } catch (JsonProcessingException e) {
                        log.error("Error creating JSON message", e);
                    }
                    webSocketClient.notifyClients(messageJson);
                    log.info("Challenge triggered because user ID mismatch.");
                    List<List<OrderVo>> result = fetchAndEncapsulateOrderData(userId);
                    return new R(CHALLENGE_SUCCESS_CODE, "Challenge succeeded.", result, null);
                }
            } catch (ExpiredJwtException e) {
                log.error("Token has expired", e);
                return R.fail("Token has expired");
            } catch (JwtException e) {
                log.error("Error processing token", e);
                return R.fail("Token is invalid");
            }
        }

        // If the Token is legal and the userId matches, return the list of data.
        List<List<OrderVo>> result = fetchAndEncapsulateOrderData(userId);
        return R.ok("Data retrieved successfully.", result);
    }

    private List<List<OrderVo>> fetchAndEncapsulateOrderData(Integer userId) {
        QueryWrapper<Order> orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.eq("user_id", userId);
        List<Order> orderList = orderMapper.selectList(orderQueryWrapper);

        Set<Integer> productIds = new HashSet<>();
        for (Order order : orderList) {
            productIds.add(order.getProductId());
        }

        ProductIdsParam productIdsParam = new ProductIdsParam();
        productIdsParam.setProductIds(new ArrayList<>(productIds));

        List<Product> productList = productClient.ids(productIdsParam);
        Map<Integer, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        List<List<OrderVo>> result = new ArrayList<>();
        Map<Long, List<Order>> listMap = orderList.stream()
                .collect(Collectors.groupingBy(Order::getOrderId));

        for (List<Order> orders : listMap.values()) {
            List<OrderVo> orderVos = new ArrayList<>();
            for (Order order : orders) {
                Product product = productMap.get(order.getProductId());
                OrderVo orderVo = new OrderVo();
                orderVo.setProductName(product.getProductName());
                orderVo.setProductPicture(product.getProductPicture());
                orderVo.setId(order.getId());
                orderVo.setOrderId(order.getOrderId());
                orderVo.setOrderTime(order.getOrderTime());
                orderVo.setProductNum(order.getProductNum());
                orderVo.setProductId(order.getProductId());
                orderVo.setProductPrice(order.getProductPrice());
                orderVo.setUserId(order.getUserId());
                orderVos.add(orderVo);
            }
            result.add(orderVos);
        }
        return result;
    }

    /**
     * Check if the order contains items to be deleted
     */
    @Override
    public Object check(Integer productId) {

        QueryWrapper<Order> queryWrapper
                = new QueryWrapper<>();
        queryWrapper.eq("product_id",productId);
        Long total = baseMapper.selectCount(queryWrapper);

        if (total == 0){
            return R.ok("The item to be deleted does not exist in the order!");
        }

        return R.fail("There is a product in the order to be deleted, and the deletion failed!");
    }

    @Override
    public Map<String, Object> getOrderDetail(Long orderId) {
        List<Order> orders = orderMapper.selectOrdersByOrderId(orderId);

        double totalAmount = 0;
        int totalQuantity = 0;
        String orderAddress = null;
        String orderPhone = null;
        String orderName = null;
        for (Order order : orders) {
            totalAmount += order.getProductPrice() * order.getProductNum();
            totalQuantity += order.getProductNum();
            orderAddress = order.getOrderAddress();
            orderPhone = order.getOrderPhone();
            orderName = order.getOrderName();
        }

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrderId(orderId);
        orderDetailVo.setTotalAmount(totalAmount);
        orderDetailVo.setTotalQuantity(totalQuantity);
        orderDetailVo.setOrderAddress(orderAddress);
        orderDetailVo.setOrderPhone(orderPhone);
        orderDetailVo.setOrderName(orderName);

        Map<String, Object> response = new HashMap<>();
        response.put("orderDetailVo", orderDetailVo);
        response.put("orders", orders);
        return response;
    }

    @Override
    public Map<String, Object> updateOrderDetail(Long orderId, OrderDetailVo updatedOrderDetail) {
        ObjectMapper mapper = new ObjectMapper();
        List<Order> orders = orderMapper.selectOrdersByOrderId(orderId);
        String orderAddress = null;
        String orderPhone = null;
        String orderName = null;
        Integer productNum = null;
        for (Order order : orders) {
            orderAddress = order.getOrderAddress();
            orderPhone = order.getOrderPhone();
            orderName = order.getOrderName();
            productNum = order.getProductNum();
        }

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrderId(orderId);

        if (updatedOrderDetail != null) {
            if (updatedOrderDetail.getOrderAddress() != null) {
                orderDetailVo.setOrderAddress(updatedOrderDetail.getOrderAddress());
            } else {
                orderDetailVo.setOrderAddress(orderAddress);
            }

            if (updatedOrderDetail.getOrderPhone() != null) {
                orderDetailVo.setOrderPhone(updatedOrderDetail.getOrderPhone());
            } else {
                orderDetailVo.setOrderPhone(orderPhone);
            }

            if (updatedOrderDetail.getOrderName() != null) {
                orderDetailVo.setOrderName(updatedOrderDetail.getOrderName());
            } else {
                orderDetailVo.setOrderName(orderName);
            }

            if (updatedOrderDetail.getProductNum() != null) {
                orderDetailVo.setProductNum(updatedOrderDetail.getProductNum());
                String messageJson = "";
                try {
                    messageJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
                        put("message", "Challenge succeeded: Triggered by modify number.");
                    }});
                    log.info("Sending WebSocket notification due to modify number.");
                    webSocketClient.notifyClients(messageJson);
                } catch (JsonProcessingException e) {
                    log.error("Error creating JSON message for modify number", e);
                }
            } else {
                orderDetailVo.setProductNum(productNum);
            }
        }

        orderMapper.updateOrderDetail(orderDetailVo);
        Map<String, Object> response = new HashMap<>();
        response.put("orderDetailVo", orderDetailVo);
        response.put("orders", orders);

        return response;
    }
}
