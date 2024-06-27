package com.teachingtool.user.service;

import com.teachingtool.param.PageParam;
import com.teachingtool.pojo.User;
import com.teachingtool.utils.R;

import java.util.List;

public interface UserService{


    /**
     * 检查账号是否可用
     * @param userName
     * @return
     */
    R check(String userName);

    /**
     * 进行账号注册
     * @param user 参数没有校验
     * @return
     */
    R register(User user);

    /**
     * 进行账号登录
     * @param user
     * @return
     */
    R login(User user);

}
