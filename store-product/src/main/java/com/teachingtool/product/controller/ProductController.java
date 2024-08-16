package com.teachingtool.product.controller;

import com.teachingtool.param.ProductIdsParam;
import com.teachingtool.pojo.Product;
import com.teachingtool.product.param.ProductParamInteger;
import com.teachingtool.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("ids")
    public List<Product> list(@RequestBody ProductIdsParam productIdsParam){
        return productService.ids(productIdsParam);
    }

    @PostMapping("all")
    public Object all(@RequestBody ProductParamInteger productParamInteger){
        return productService.all(productParamInteger);
    }

    @PostMapping("detail")
    public Object detail(@RequestBody Map<String,Integer> param){
        Integer productID = param.get("productID");
        return productService.detail(productID);
    }

    @PostMapping("pictures")
    public Object productPictures(@RequestBody Map<String,Integer> param){
        Integer productID = param.get("productID");
        return productService.pictures(productID);
    }
}
