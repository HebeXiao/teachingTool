package com.teachingtool.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachingtool.param.ProductIdsParam;
import com.teachingtool.pojo.Product;
import com.teachingtool.product.param.ProductParamInteger;

import java.util.List;

public interface ProductService extends IService<Product> {

    List<Product> ids(ProductIdsParam productIdsParam);

    Object byCategory(ProductParamInteger productParamInteger);

    Object all(ProductParamInteger productParamInteger);

    Object detail(Integer productID);

    Object pictures(Integer productID);
}

