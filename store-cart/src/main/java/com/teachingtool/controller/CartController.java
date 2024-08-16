package com.teachingtool.controller;

import com.teachingtool.param.CartParam;
import com.teachingtool.service.CartService;
import com.teachingtool.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("save")
    public R save(@RequestBody CartParam cartParam){
        return cartService.save(cartParam);
    }

    @PostMapping("list")
    public R list(@RequestBody CartParam cartParam){
        return cartService.list(cartParam);
    }

    @GetMapping("/list/new")
    public R listCartByUserId(@RequestParam("user_id") Integer userId,
                                @RequestHeader(value = "Authorization", required = false, defaultValue = "") String token) {
        return cartService.listById(userId, token);
    }


    @PostMapping("update")
    public R update(@RequestBody CartParam cartParam){
        return cartService.update(cartParam);
    }


    @PostMapping("remove")
    public R remove(@RequestBody CartParam cartParam){
        return cartService.remove(cartParam);
    }

    @PostMapping("check")
    public R check(@RequestBody Integer productId){
        return cartService.check(productId);
    }
}
