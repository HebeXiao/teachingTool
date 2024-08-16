package com.teachingtool.user.service;

import com.teachingtool.param.PageParam;
import com.teachingtool.pojo.User;
import com.teachingtool.utils.R;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface UserService{

    R check(String userName);

    R register(User user);

    R login(User user);

    User getUserById(Integer userId);

    @Transactional
    User updateUserInfo(Map<String, Object> userInfo);

    Map<String, Object> getUserAddress(Map<String, Integer> request);

    Map<String, Object> saveUserAddress(Map<String, Object> request);
}
