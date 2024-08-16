package com.teachingtool.clients;

import com.teachingtool.param.ProductIdsParam;
import com.teachingtool.pojo.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;

@FeignClient(value = "product-service")
public interface ProductClient {

    @GetMapping("/product/list")
    List<Product> list();

    @PostMapping("/product/ids")
    List<Product> ids(@RequestBody ProductIdsParam productIdsParam);
}