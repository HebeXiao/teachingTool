package com.teachingtool.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderDetailVo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @JsonProperty("order_id")
    private Long    orderId;
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("product_id")
    private Integer productId;
    @JsonProperty("product_num")
    private Integer productNum;
    @JsonProperty("product_price")
    private Double  productPrice;
    @JsonProperty("order_time")
    private Long    orderTime;
    @JsonProperty("order_address")
    private String  orderAddress;
    @JsonProperty("order_phone")
    private String  orderPhone;
    @JsonProperty("order_name")
    private String  orderName;
    private Double totalAmount;
    private Integer totalQuantity;
}
