package com.teachingtool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachingtool.clients.ProductClient;
import com.teachingtool.mapper.CartMapper;
import com.teachingtool.param.CartParam;
import com.teachingtool.param.ProductIdsParam;
import com.teachingtool.pojo.Cart;
import com.teachingtool.pojo.Product;
import com.teachingtool.service.CartService;
import com.teachingtool.utils.R;
import com.teachingtool.vo.CartVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private ProductClient productClient;

    @Autowired
    private CartMapper cartMapper;

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
        queryWrapper.eq("user_id",cartParam.getUserId());
        queryWrapper.eq("product_id",cartParam.getProductId());
        Cart cart = cartMapper.selectOne(queryWrapper);
        if (cart != null){
            //If it's not the first time, just go back to Added!
            cart.setNum(cart.getNum()+1);
            cartMapper.updateById(cart);
            R ok = R.ok("The item is already in the shopping cart, quantity +1!");
            ok.setCode("002");
            return ok;
        }

        cart = new Cart();
        cart.setNum(1);
        cart.setProductId(cartParam.getProductId());
        cart.setUserId(cartParam.getUserId());
        cartMapper.insert(cart);

        CartVo cartVo = new CartVo(product,cart);
        log.info("CartServiceImpl.save operation ends, result:{}",cartVo);
        return R.ok(cartVo);
    }

    /**
     * Query Shopping Cart Data Collection
     */
    @Override
    public R list(CartParam cartParam) {
        //获取用户id
        Integer userId = cartParam.getUserId();
        //查询用户id对应的购物车数据
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<Cart> cartList = cartMapper.selectList(queryWrapper);
        if (cartList == null || cartList.size() == 0){
            return R.ok("No data for the shopping cart!",cartList);
        }
        //封装商品集合,查询商品数据
        List<Integer> ids = new ArrayList<>();
        for (Cart cart : cartList) {
            ids.add(cart.getProductId());
        }

        ProductIdsParam productIdsParam = new ProductIdsParam();
        productIdsParam.setProductIds(ids);

        List<Product> productList = productClient.ids(productIdsParam);
        //集合转map!
        Map<Integer, Product> map = productList.stream().collect(Collectors.
                toMap(Product::getProductId, v -> v));
        System.out.println("map = " + map);
        //结果封装即可
        List<CartVo> list = new ArrayList<>(cartList.size());
        for (Cart cart : cartList) {
            CartVo cartVo = new CartVo(map.get(cart.getProductId()),cart);
            list.add(cartVo);
        }

        R ok = R.ok(list);
        log.info("CartServiceImpl.list业务结束，结果:{}",ok);
        return ok;
    }

    /**
     * 修改购物车数量
     *
     * @param cartParam
     * @return
     */
    @Override
    public R update(CartParam cartParam) {

        //1.查询商品对应的详情
        List<Integer> ids = new ArrayList<>();
        ids.add(cartParam.getProductId());
        ProductIdsParam productIdsParam = new ProductIdsParam();
        productIdsParam.setProductIds(ids);

        //3.数据修改
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",cartParam.getUserId());
        queryWrapper.eq("product_id",cartParam.getProductId());
        Cart cart = cartMapper.selectOne(queryWrapper);
        cart.setNum(cartParam.getNum());
        cartMapper.updateById(cart);

        //4.结果封装
        R ok = R.ok("Shopping cart items updated successfully!");
        log.info("CartServiceImpl.update业务结束，结果:{}",ok);
        return ok;
    }

    /**
     * 移除购物车数据
     */
    @Override
    public R remove(CartParam cartParam) {

        //删除参数封装
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",cartParam.getUserId());
        queryWrapper.eq("product_id",cartParam.getProductId());
        //删除数据
        cartMapper.delete(queryWrapper);
        return R.ok("Delete items successfully!");
    }

    /**
     * 检查商品是否存在
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
