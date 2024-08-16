package com.teachingtool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachingtool.clients.ProductClient;
import com.teachingtool.clients.WebSocketClient;
import com.teachingtool.mapper.CartMapper;
import com.teachingtool.param.CartParam;
import com.teachingtool.param.ProductIdsParam;
import com.teachingtool.pojo.Cart;
import com.teachingtool.pojo.Product;
import com.teachingtool.service.CartService;
import com.teachingtool.utils.R;
import com.teachingtool.vo.CartVo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private ProductClient productClient;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private WebSocketClient webSocketClient;

    /**
     * Add Shopping Cart
     */
    @Override
    public R save(CartParam cartParam) {
        List<Integer> ids = new ArrayList<>();
        ids.add(cartParam.getProductId());
        ProductIdsParam productIdsParam = new ProductIdsParam();
        productIdsParam.setProductIds(ids);
        List<Product> productList = productClient.ids(productIdsParam);

        Product product = productList.get(0);
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", cartParam.getUserId())
                .eq("product_id", cartParam.getProductId());
        Cart cart = cartMapper.selectOne(queryWrapper);
        if (cart != null){
            // Product already in cart, increase quantity
            cart.setNum(cart.getNum()+1);
            cartMapper.updateById(cart);
            return R.ok("The item is already in the shopping cart, add quantity!");
        }

        cart = new Cart();
        cart.setNum(1);
        cart.setProductId(cartParam.getProductId());
        cart.setUserId(cartParam.getUserId());
        cartMapper.insert(cart);

        CartVo cartVo = new CartVo(product,cart);
        return R.ok(cartVo);
    }

    /**
     * Query Shopping Cart Data Collection
     */
    @Override
    public R list(CartParam cartParam) {
        Integer userId = cartParam.getUserId();
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<Cart> cartList = cartMapper.selectList(queryWrapper);

        if (cartList == null || cartList.isEmpty()) {
            return R.ok("No data for the shopping cart!", Collections.emptyList());
        }

        List<Integer> ids = cartList.stream()
                .map(Cart::getProductId)
                .collect(Collectors.toList());

        ProductIdsParam productIdsParam = new ProductIdsParam();
        productIdsParam.setProductIds(ids);
        List<Product> productList = productClient.ids(productIdsParam);

        Map<Integer, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        List<CartVo> resultList = cartList.stream()
                .map(cart -> new CartVo(productMap.get(cart.getProductId()), cart))
                .collect(Collectors.toList());

        return R.ok(resultList);
    }

    /**
     * Order Data Inquiry Service
     */
    public static final String CHALLENGE_SUCCESS_CODE = "999";
    private static final String SECRET_KEY = "your_secret_key";

    @Override
    public R listById(Integer userId, String token) {
        ObjectMapper mapper = new ObjectMapper();

        // Token verification
        if (token == null || token.isEmpty()) {
            return R.fail("Token is empty.");
        } else if (!token.startsWith("Bearer ")) {
            return R.fail("Token is invalid");
        } else {
            token = token.substring(7); // Remove the Bearer prefix.
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                // Check if the Token has expired
                if (claims.getExpiration().before(new Date())) {
                    return R.fail("Token has expired");
                }

                Integer tokenUserId = claims.get("userId", Integer.class);
                if (!userId.equals(tokenUserId)) {
                    String messageJson = "";
                    try {
                        messageJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
                            put("userID", userId);
                            put("message", "Challenge succeeded: Triggered by user ID mismatch in cart.");
                        }});
                        log.info(messageJson);
                    } catch (JsonProcessingException e) {
                        log.error("Error creating JSON message", e);
                    }
                    webSocketClient.notifyClients(messageJson); // 发送 JSON 格式的消息
                    log.info("Challenge triggered because user ID mismatch in cart.");
                    List<CartVo> result = fetchAndEncapsulateCartData(userId);
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
        List<CartVo> result = fetchAndEncapsulateCartData(userId);
        return R.ok("Data retrieved successfully.", result);
    }

    private List<CartVo> fetchAndEncapsulateCartData(Integer userId) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<Cart> cartList = cartMapper.selectList(queryWrapper);

        // Return an empty list if the cart is empty
        if (cartList == null || cartList.isEmpty()) {
            return new ArrayList<>();
        }

        // Extract all product IDs
        List<Integer> productIds = cartList.stream()
                .map(Cart::getProductId)
                .distinct()
                .collect(Collectors.toList());

        // Retrieve product details in a batch request
        ProductIdsParam productIdsParam = new ProductIdsParam();
        productIdsParam.setProductIds(productIds);
        List<Product> productList = productClient.ids(productIdsParam);
        Map<Integer, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // Encapsulate the result list
        return cartList.stream()
                .map(cart -> new CartVo(productMap.get(cart.getProductId()), cart))
                .collect(Collectors.toList());
    }

    /**
     * Modify Shopping Cart Quantity
     */
    @Override
    public R update(CartParam cartParam) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", cartParam.getUserId())
                .eq("product_id", cartParam.getProductId());

        Cart cart = cartMapper.selectOne(queryWrapper);

        cart.setNum(cartParam.getNum());
        cartMapper.updateById(cart);

        return R.ok("Shopping cart items updated successfully!");
    }


    /**
     * Remove Shopping Cart Data
     */
    @Override
    public R remove(CartParam cartParam) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",cartParam.getUserId());
        queryWrapper.eq("product_id",cartParam.getProductId());
        cartMapper.delete(queryWrapper);
        return R.ok("Delete items successfully!");
    }

    /**
     * Check for the presence of goods
     */
    @Override
    public R check(Integer productId) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id",productId);
        Long total = cartMapper.selectCount(queryWrapper);

        if (total == 0L){
            return R.ok("The item to be deleted does not exist in the shopping cart!");
        }

        return R.fail("The item to be deleted exists in the shopping cart!");
    }

}
