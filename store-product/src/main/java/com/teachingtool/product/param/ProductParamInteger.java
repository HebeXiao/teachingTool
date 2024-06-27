package com.teachingtool.product.param;

import lombok.Data;

import java.util.List;

@Data
public class ProductParamInteger {

    private List<Integer> categoryID;
    private int currentPage = 1; //默认值
    private int pageSize = 15; //默认值

}
