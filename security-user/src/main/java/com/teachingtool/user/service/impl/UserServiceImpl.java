package com.teachingtool.user.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachingtool.clients.WebSocketClient;
import com.teachingtool.pojo.User;
import com.teachingtool.user.service.UserService;
import com.teachingtool.utils.MD5Util;
import com.teachingtool.utils.R;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teachingtool.user.constants.UserConstants;
import com.teachingtool.user.mapper.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String SECRET_KEY = "your_secret_key";
    private static final long EXPIRATION_TIME = 86400000; // 1 day in milliseconds

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketClient webSocketClient;

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
        user.setMembership(false);

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

    @Override
    public User getUserById(Integer userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @Transactional
    public User updateUserInfo(Map<String, Object> userInfo) {
        ObjectMapper mapper = new ObjectMapper();
        Integer userId = (Integer) userInfo.get("user_id");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        String newPhoneNumber = (String) userInfo.get("user_phonenumber");
        String newLinkman = (String) userInfo.get("linkman");
        String newAddress = (String) userInfo.get("address");
        Object membershipObj = userInfo.get("membership");

        Boolean newMembership = null;
        if (membershipObj != null) {
            if (membershipObj instanceof Boolean) {
                newMembership = (Boolean) membershipObj;
            } else {
                // 触发WebSocket，提示类型有问题
                String messageJson = "";
                try {
                    messageJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
                        put("userID", userId);
                        put("message", "Challenge succeeded: Triggered by invalid membership type.");
                    }});
                    log.info("Sending WebSocket notification due to invalid membership type.");
                    webSocketClient.notifyClients(messageJson);
                } catch (JsonProcessingException e) {
                    log.error("Error creating JSON message for WebSocket notification", e);
                }
                return null;
            }
        }

        boolean isUpdated = false;  // 标志是否有更新发生

        if (newPhoneNumber != null && !newPhoneNumber.equals(user.getUserPhonenumber())) {
            user.setUserPhonenumber(newPhoneNumber);
            isUpdated = true;
        }
        if (newLinkman != null && !newLinkman.equals(user.getLinkman())) {
            user.setLinkman(newLinkman);
            isUpdated = true;
        }
        if (newAddress != null && !newAddress.equals(user.getAddress())) {
            user.setAddress(newAddress);
            isUpdated = true;
        }
        if (newMembership != null) {
            user.setMembership(newMembership);
            log.info("Member attributes have been modified by unauthorized persons");
            String messageJson = "";
            try {
                messageJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
                    put("userID", userId);
                    put("message", "Challenge succeeded: Triggered by unauthorized modification.");
                }});
                log.info("Sending WebSocket notification due to unauthorized modification.");
                webSocketClient.notifyClients(messageJson);
            } catch (JsonProcessingException e) {
                log.error("Error creating JSON message for WebSocket notification", e);
            }
            isUpdated = true;
        }

        if (isUpdated) {
            int updatedRows = userMapper.updateById(user);
            if (updatedRows > 0) {
                // 在返回之前清除密码字段
                user.setPassword(null);
                return user;
            }
        }

        return null;
    }





    @Override
    public Map<String, Object> getUserAddress(Map<String, Integer> request) {
        Integer userId = request.get("user_id");
        User address = userMapper.selectById(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", address);
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> saveUserAddress(Map<String, Object> userInfo) {
        Integer userId = (Integer) userInfo.get("user_id");
        Map<String, String> addressInfo = (Map<String, String>) userInfo.get("address");

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setLinkman(addressInfo.get("linkman"));
            user.setUserPhonenumber(addressInfo.get("userPhonenumber"));
            log.info(addressInfo.get("userPhonenumber"));
            user.setAddress(addressInfo.get("address"));
            userMapper.updateById(user);

            Map<String, Object> response = new HashMap<>();
            response.put("code", "001");
            response.put("msg", "Address updated successfully");
            response.put("data", addressInfo);
            return response;
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("code", "002");
            response.put("msg", "Address update failure");
            return response;
        }
    }
}
