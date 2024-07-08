package com.teachingtool.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@TableName("user")
@Data
public class User implements Serializable {

    public static final Long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @JsonProperty("user_id")
    private Integer userId;
    private String  userName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String  password;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String  userPhonenumber;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String linkman;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String address;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean membership;
}

