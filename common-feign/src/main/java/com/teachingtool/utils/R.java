package com.teachingtool.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class R  implements Serializable {

    public static final Long serialVersionUID = 1L;

    /**
     * Generic Success Status Code
     */
    public static final String SUCCESS_CODE = "001";
    /**
     * Failure status code
     */
    public static final String FAIL_CODE = "004";
    /**
     * Not logged in
     */
    public static final String USER_NO_LOGIN = "401";

    private String code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String msg;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long   total;

    /**
     * Success
     */
    public static R ok(String msg,Object data,Long total){
        return new R(SUCCESS_CODE,msg,data,total);
    }

    /**
     * Success
     */
    public static R ok(String msg,Object data){
        return ok(msg,data,null);
    }

    /**
     * Success
     */
    public static R ok(String msg){
        return ok(msg,null);
    }

    /**
     * Success
     */
    public static R ok(Object data){
        return ok(null,data);
    }

    /**
     * failure
     */
    public static R fail(String msg,Object data,Long total){
        return new R(FAIL_CODE,msg,data,total);
    }

    /**
     * failure
     */
    public static R fail(String msg,Object data){
        return fail(msg,data,null);
    }

    /**
     * failure
     */
    public static R fail(String msg){
        return fail(msg,null);
    }
}
