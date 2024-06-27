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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
     * 类别商品查询 前端传递类别集合
     *
     * @param productParamInteger
     * @return
     */
    @Cacheable(value = "list.product",key =
            "#productParamInteger.categoryID+" +
                    "'-'+#productParamInteger.currentPage+" +
                    "'-'+#productParamInteger.pageSize")
    @Override
    public Object byCategory(ProductParamInteger productParamInteger) {

        //1.拆分请求参数
        List<Integer> categoryID = productParamInteger.getCategoryID();
        int currentPage = productParamInteger.getCurrentPage();
        int pageSize = productParamInteger.getPageSize();
        //2.请求条件封装
        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        if (categoryID != null && categoryID.size() > 0){
            productQueryWrapper.in("category_id",categoryID);
        }
        IPage<Product> page = new Page<>(currentPage,pageSize);
        //3.数据查询
        IPage<Product> iPage = productMapper.selectPage(page, productQueryWrapper);
        //4.结果封装
        List<Product> productList = iPage.getRecords();
        long total = iPage.getTotal();

        R ok = R.ok(null, productList, total);

        log.info("ProductServiceImpl.byCategory业务结束，结果:{}",ok);
        return ok;
    }

    /**
     * 全部商品查询,可以进行类别集合数据查询业务复用
     *
     * @param productParamInteger
     * @return
     */
    @Cacheable(value = "list.product",key ="#productParamInteger.currentPage+" +
            "'-'+#productParamInteger.pageSize")
    @Override
    public Object all(ProductParamInteger productParamInteger) {

        return byCategory(productParamInteger);
    }

    /**
     * 查询商品详情
     *
     * @param productID 商品id
     * @return
     */
    @Override
    @Cacheable(value = "product",key = "#productID")
    public Object detail(Integer productID) {

        Product product = productMapper.selectById(productID);

        R ok = R.ok(product);

        log.info("ProductServiceImpl.detail业务结束，结果:{}",ok);

        return ok;
    }

    /**
     * 查询商品图片
     *
     * @param productID
     * @return
     */
    @Cacheable(value = "picture",key = "#productID")
    @Override
    public Object pictures(Integer productID) {

        //参数封装
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id",productID);
        //数据库查询
        List<Picture> pictureList = pictureMapper.selectList(queryWrapper);
        //结果封装
        R r = R.ok(pictureList);

        log.info("ProductServiceImpl.pictures业务结束，结果:{}",r);

        return r;
    }

    /**
     * 查询全部商品信息
     *
     * @return
     */
    @Override
    public List<Product> list() {

        List<Product> products = productMapper.selectList(null);

        return products;
    }

    /**
     * 查询商品集合
     * @param productIdsParam
     * @return
     */
    @Cacheable(value = "list.product", key = "#productIdsParam.productIds")
    @Override
    public List<Product> ids(ProductIdsParam productIdsParam) {
        List<Integer> productIds = productIdsParam.getProductIds();

        // 检查productIds列表是否为空
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList(); // 如果为空，直接返回空列表
        }

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("product_id", productIds);

        return productMapper.selectList(queryWrapper);
    }

}

