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


    /**
     * 查询全部商品信息,供search服务更新
     * @return
     */
    @GetMapping("list")
    public List<Product> list(){

        return productService.list();
    }


    /**
     * 供收藏服务使用,根据传入的id,查询商品集合!
     * @return
     */
    @PostMapping("ids")
    public List<Product> list(@RequestBody ProductIdsParam productIdsParam){

        return productService.ids(productIdsParam);
    }

    /**
     * 类别查询
     * @param productParamInteger
     * @return
     */
    @PostMapping("bycategory")
    public Object byCategory(@RequestBody ProductParamInteger productParamInteger){

        return productService.byCategory(productParamInteger);
    }

    /**
     * 查询全部商品,可以复用业务!
     * @param productParamInteger
     * @return
     */
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
