package com.teachingtool.user.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.teachingtool.param.PageParam;
import com.teachingtool.pojo.User;
import com.teachingtool.user.service.UserService;
import com.teachingtool.utils.MD5Util;
import com.teachingtool.utils.R;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teachingtool.user.constants.UserConstants;
import com.teachingtool.user.mapper.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String SECRET_KEY = "your_secret_key";
    private static final long EXPIRATION_TIME = 86400000; // 1 day in milliseconds

    @Autowired
    private UserMapper userMapper;

    /**
     * 检查账号是否可用
     *
     * @param userName
     * @return
     */
    @Override
    public R check(String userName) {

        //1.账号非空校验
        if (StringUtils.isEmpty(userName)){
            log.info("UserServiceImpl.check业务开始，参数:{}",userName);
            return R.fail("Account is null, not available!");
        }
        //2.数据库查询
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name",userName);
        Long count = userMapper.selectCount(queryWrapper);

        //3.结果处理
        log.info("UserServiceImpl.check业务结束，结果:{}",count);

        if (count > 0 ){

            return R.fail("Account already exists, unavailable!");
        }

        return R.ok("Account doesn't exist, you can use it!");
    }

    /**
     * 进行账号注册
     *
     * @param user 参数没有校验
     * @return
     */
    @Override
    public R register(User user) {

        //1.参数校验
        if (StringUtils.isEmpty(user.getUserName()) || StringUtils.isEmpty(user.getPassword()))
        {
            log.info("UserServiceImpl.register业务结束，结果:{}",user);
            return R.fail("Username or password is null, registration failed!");
        }
        //2.数据库查询
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name",user.getUserName());
        Long count = userMapper.selectCount(queryWrapper);

        if (count>0) {
            log.info("UserServiceImpl.register业务结束，结果:{}",count);
            return R.fail("Username already exists, unavailable!");
        }
        //3.数据库插入

        //代码加密处理,注意加盐,生成常量
        String newPwd = MD5Util.encode(user.getPassword() + UserConstants.USER_SLAT);

        user.setPassword(newPwd);

        int rows = userMapper.insert(user);
        //4.结果处理
        if (rows > 0){
            log.info("UserServiceImpl.register业务结束，注册成功,结果:{}",rows);
            return R.ok("Registration Successful!");
        }

        return R.fail("Account already exists, unavailable!");
    }

    /**
     * 进行账号登录
     *
     * @param user
     * @return
     */
    @Override
    public R login(User user) {
        if (StringUtils.isEmpty(user.getUserName()) || StringUtils.isEmpty(user.getPassword())) {
            log.info("UserServiceImpl.login业务结束，结果:{}", user);
            return R.fail("Username or password is null, login failed!");
        }

        String newPwd = MD5Util.encode(user.getPassword() + UserConstants.USER_SLAT);
        user.setPassword(newPwd);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", user.getUserName());
        queryWrapper.eq("password", user.getPassword());

        User loginUser = userMapper.selectOne(queryWrapper);

        if (loginUser == null) {
            log.info("UserServiceImpl.login业务结束，登录失败,结果:{}", loginUser);
            return R.fail("Wrong username or password, login failed!");
        }

        // 生成JWT token
        String token = generateToken(loginUser);

        // 设置为null,配合NoN_NULL注解,不返回给前端
        loginUser.setPassword(null);

        // 将 token 和 user 信息放入 Map 中
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("token", token);
        resultData.put("user", loginUser);

        log.info("UserServiceImpl.login业务结束，登录成功,结果:{}", loginUser);
        return R.ok("Login Successful!", resultData);
    }

    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userName", user.getUserName());
        claims.put("userId", user.getUserId());
        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }
}
