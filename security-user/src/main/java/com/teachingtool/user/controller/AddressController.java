package com.teachingtool.user.controller;

import com.teachingtool.param.AddressParam;
import com.teachingtool.utils.R;
import com.teachingtool.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping("list")
    public R list(@RequestBody Map<String,Integer> params){
        Integer userId = params.get("user_id");
        return addressService.list(userId);
    }


    @PostMapping("save")
    public R save(@RequestBody AddressParam address){

        return addressService.save(address);
    }


    @PostMapping("remove")
    public R remove(@RequestBody Map<String,Integer> params){
        Integer id = params.get("id");
        return addressService.remove(id);
    }


}

