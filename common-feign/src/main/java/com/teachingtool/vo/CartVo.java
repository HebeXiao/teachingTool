package com.teachingtool.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.teachingtool.pojo.Cart;
import com.teachingtool.pojo.Product;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class CartVo implements Serializable {

    private Integer id;
    private Integer productID;
    private String  productName;
    private String  productImg;
    private Double price;
    private Integer num;
    private Integer maxNum;
    private Boolean check = false;

    public CartVo(Product product, Cart cart) {
        this.id = cart.getId();
        this.productID = product.getProductId();
        this.productName = product.getProductName();
        this.productImg = product.getProductPicture();
        this.price = product.getProductSellingPrice();
        this.num = cart.getNum();
        this.maxNum = product.getProductNum();
        this.check = false;
    }
}

