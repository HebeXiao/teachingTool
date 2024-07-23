package com.teachingtool.user.controller;

import com.teachingtool.param.PageParam;
import com.teachingtool.pojo.User;
import com.teachingtool.user.service.UserService;
import com.teachingtool.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("check")
    public R check(@RequestBody User user){

        return userService.check(user.getUserName());
    }


    @PostMapping("register")
    public R register(@RequestBody User user){

        return userService.register(user);
    }


    @PostMapping("login")
    public R login(@RequestBody User user){

        return userService.login(user);
    }

    @PostMapping("/info")
    public ResponseEntity<User> getUserInfo(@RequestBody Map<String, Integer> request) {
        Integer userId = request.get("user_id");
        User user = userService.getUserById(userId);
        if (user != null) {
            user.setPassword(null); // 不返回密码
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Object> updateUserInfo(@RequestBody Map<String, Object> request) {
        User updatedUser = userService.updateUserInfo(request);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);  // 返回更新后的用户信息
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/address/list")
    public ResponseEntity<Map<String, Object>> getUserAddress(@RequestBody Map<String, Integer> request) {
        Map<String, Object> address = userService.getUserAddress(request);
        return ResponseEntity.ok(address);
    }

    @PostMapping("/address/save")
    public ResponseEntity<Map<String, Object>> saveUserAddress(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = userService.saveUserAddress(request);
        if ("001".equals(response.get("code"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
