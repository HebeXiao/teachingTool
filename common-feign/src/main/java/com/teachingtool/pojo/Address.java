package com.teachingtool.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("address")
public class Address {

    @TableId(type = IdType.AUTO)
    private Integer id;


    private String address;
    private String linkman;
    private String phone;
    @TableField("user_id")
    private Integer userId;

}
