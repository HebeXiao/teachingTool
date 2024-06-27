package com.teachingtool.user.controller;

import com.teachingtool.param.PageParam;
import com.teachingtool.pojo.User;
import com.teachingtool.user.service.UserService;
import com.teachingtool.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
