package com.teachingtool.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachingtool.param.ProductIdsParam;
import com.teachingtool.pojo.Picture;
import com.teachingtool.pojo.Product;
import com.teachingtool.product.mapper.PictureMapper;
import com.teachingtool.product.mapper.ProductMapper;
import com.teachingtool.product.param.ProductParamInteger;
import com.teachingtool.product.service.ProductService;
import com.teachingtool.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PictureMapper pictureMapper;

    /**
     * All Product Inquiry
     */
    @Cacheable(value = "list.product", key =
            "#productParamInteger.categoryID != null ? " +
                    "#productParamInteger.categoryID+'-'+#productParamInteger.currentPage+'-'+#productParamInteger.pageSize : " +
                    "#productParamInteger.currentPage+'-'+#productParamInteger.pageSize")
    @Override
    public Object all(ProductParamInteger productParamInteger) {
        List<Integer> categoryID = productParamInteger.getCategoryID();
        int currentPage = productParamInteger.getCurrentPage();
        int pageSize = productParamInteger.getPageSize();

        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        if (categoryID != null && !categoryID.isEmpty()) {
            productQueryWrapper.in("category_id", categoryID);
        }
        IPage<Product> page = new Page<>(currentPage, pageSize);
        IPage<Product> iPage = productMapper.selectPage(page, productQueryWrapper);
        List<Product> productList = iPage.getRecords();
        long total = iPage.getTotal();

        return R.ok(null, productList, total);
    }


    /**
     * Inquiry Product Details
     */
    @Override
    @Cacheable(value = "product",key = "#productID")
    public Object detail(Integer productID) {
        Product product = productMapper.selectById(productID);
        return R.ok(product);
    }

    /**
     * Inquiry product picture
     */
    @Cacheable(value = "picture",key = "#productID")
    @Override
    public Object pictures(Integer productID) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id",productID);
        List<Picture> pictureList = pictureMapper.selectList(queryWrapper);
        return R.ok(pictureList);
    }

    /**
     * Search Product Collection
     */
    @Cacheable(value = "list.product", key = "#productIdsParam.productIds")
    @Override
    public List<Product> ids(ProductIdsParam productIdsParam) {
        List<Integer> productIds = productIdsParam.getProductIds();

        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("product_id", productIds);
        return productMapper.selectList(queryWrapper);
    }
}

