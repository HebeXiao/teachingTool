package com.teachingtool.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachingtool.clients.ProductClient;
import com.teachingtool.clients.WebSocketClient;
import com.teachingtool.order.mapper.OrderMapper;
import com.teachingtool.order.service.OrderService;
import com.teachingtool.param.OrderParam;
import com.teachingtool.param.ProductIdsParam;
import com.teachingtool.param.ProductNumberParam;
import com.teachingtool.pojo.Order;
import com.teachingtool.pojo.Product;
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
    private ProductClient productClient;

    /**
     * 消息队列发送
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WebSocketClient webSocketClient;

    /**
     * 订单保存业务
     * 库存和购物车使用mq异步,避免分布式事务!
     * @param orderParam
     * @return
     */
    @Transactional //添加事务
    @Override
    public Object save(OrderParam orderParam) {

        //修改清空购物车的参数
        List<Integer> cartIds = new ArrayList<>();
        //修改批量插入数据库的参数
        List<Order>  orderList = new ArrayList<>();
        //商品修改库存参数集合
        List<ProductNumberParam>  productNumberParamList  =
                new ArrayList<>();

        Integer userId = orderParam.getUserId();
        List<CartVo> products = orderParam.getProducts();
        //封装order实体类集合
        //统一生成订单编号和创建时间
        //使用时间戳 + 做订单编号和事件
        long ctime = System.currentTimeMillis();

        for (CartVo cartVo : products) {
            cartIds.add(cartVo.getId()); //进行购物车订单保存
            //订单信息保存
            Order order = new Order();
            order.setOrderId(ctime);
            order.setUserId(userId);
            order.setOrderTime(ctime);
            order.setProductId(cartVo.getProductID());
            order.setProductNum(cartVo.getNum());
            order.setProductPrice(cartVo.getPrice());
            orderList.add(order); //添加用户信息

            //修改信息存储
            ProductNumberParam productNumberParam = new ProductNumberParam();
            productNumberParam.setProductId(cartVo.getProductID());
            productNumberParam.setProductNum(cartVo.getNum());
            productNumberParamList.add(productNumberParam); //添加集合
        }
        //批量数据插入
        this.saveBatch(orderList); //批量保存

        //修改商品库存 [product-service] [异步通知]
        /**
         *  交换机: topic.ex
         *  routingkey: sub.number
         *  消息: 商品id和减库存数据集合
         */
        rabbitTemplate.convertAndSend("topic.ex","sub.number",productNumberParamList);
        //清空对应购物车数据即可 [注意: 不是清空用户所有的购物车数据] [cart-service] [异步通知]
        /**
         * 交换机:topic.ex
         * routingkey: clear.cart
         * 消息: 要清空的购物车id集合
         */
        rabbitTemplate.convertAndSend("topic.ex","clear.cart",cartIds);

        R ok = R.ok("Order Generated Successfully!");
        return ok;
    }

    /**
     * 订单数据查询业务
     */
    public static final String CHALLENGE_SUCCESS_CODE = "999";

    @Override
    public R list(Integer userId, String token) {
        ObjectMapper mapper = new ObjectMapper(); // JSON 处理器
        log.info("token: {}", token);
        // Token验证
        if (token == null || token.isEmpty()) {
            // 触发 WebSocket 逻辑，因为 token 为空
            String messageJson = "";
            try {
                // 创建包含 userID 和消息的 JSON 字符串
                messageJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
                    put("userID", userId);
                    put("message", "Challenge succeeded: Triggered by empty token.");
                }});
                log.info("Sending WebSocket notification due to empty token.");
                webSocketClient.notifyClients(messageJson); // 发送 JSON 格式的消息
            } catch (JsonProcessingException e) {
                log.error("Error creating JSON message for empty token", e);
            }
            return R.fail("Token is empty.");
        } else if (!token.startsWith("Bearer ")) {
            return R.fail("Token is invalid");
        } else {
            token = token.substring(7); // 去掉Bearer前缀
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                // 检查Token是否已过期
                Date expiration = claims.getExpiration();
                if (expiration.before(new Date())) {
                    return R.fail("Token has expired");
                }

                Integer tokenUserId = claims.get("userId", Integer.class);
                if (!userId.equals(tokenUserId)) {
                    // 触发挑战成功逻辑
                    String messageJson = "";
                    try {
                        // 创建包含 userID 和消息的 JSON 字符串
                        messageJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
                            put("userID", userId);
                            put("message", "Challenge succeeded: Triggered by user ID mismatch.");
                        }});
                        log.info(messageJson);
                    } catch (JsonProcessingException e) {
                        log.error("Error creating JSON message", e);
                    }
                    webSocketClient.notifyClients(messageJson); // 发送 JSON 格式的消息
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

        // 如果Token合法且userId匹配，返回数据列表
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
     * 检查订单是否包含要删除的商品
     *
     * @param productId
     * @return
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
        // 获取订单项
        List<Order> orders = orderMapper.selectOrdersByOrderId(orderId);

        // 计算总额和总数量
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
        // 封装订单汇总信息
        orderDetailVo.setOrderId(orderId);
        orderDetailVo.setTotalAmount(totalAmount);
        orderDetailVo.setTotalQuantity(totalQuantity);
        orderDetailVo.setOrderAddress(orderAddress);
        orderDetailVo.setOrderPhone(orderPhone);
        orderDetailVo.setOrderName(orderName);

        // 封装响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("orderDetailVo", orderDetailVo);
        response.put("orders", orders);

        // 打印返回数据
        log.info("Response: " + response);
        return response;
    }

    @Override
    public Map<String, Object> updateOrderDetail(Long orderId, OrderDetailVo updatedOrderDetail) {
        ObjectMapper mapper = new ObjectMapper(); // JSON 处理器
        // 获取订单项
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
        // 更新订单汇总信息
        orderDetailVo.setOrderId(orderId);

        // 更新orderDetailVo的数据，如果updatedOrderDetail提供了新数据
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
                // 触发 WebSocket 逻辑
                String messageJson = "";
                try {
                    // 创建包含 userID 和消息的 JSON 字符串
                    messageJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
                        put("message", "Challenge succeeded: Triggered by modify number.");
                    }});
                    log.info("Sending WebSocket notification due to modify number.");
                    webSocketClient.notifyClients(messageJson); // 发送 JSON 格式的消息
                } catch (JsonProcessingException e) {
                    log.error("Error creating JSON message for modify number", e);
                }
            } else {
                orderDetailVo.setProductNum(productNum);
            }
        }

        // 将更新后的orderDetailVo保存到数据库（假设有相应的持久化方法）
        orderMapper.updateOrderDetail(orderDetailVo);

        // 封装响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("orderDetailVo", orderDetailVo);
        response.put("orders", orders);

        // 打印返回数据
        log.info("Updated Response: " + response);
        return response;
    }



}
