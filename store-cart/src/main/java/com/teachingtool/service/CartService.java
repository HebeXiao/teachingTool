package com.teachingtool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachingtool.param.CartParam;
import com.teachingtool.pojo.Cart;
import com.teachingtool.utils.R;

public interface CartService  extends IService<Cart> {

    R save(CartParam cartParam);

    R list(CartParam cartParam);

    R listById(Integer userId, String token);

    R update(CartParam cartParam);

    R remove(CartParam cartParam);

    R check(Integer productId);
}
