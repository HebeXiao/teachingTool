package com.teachingtool.carousel.controller;

import com.teachingtool.carousel.service.CarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("carousel")
public class CarouselController  {

    @Autowired
    private CarouselService carouselService;

    /**
     * 查询首页数据,查询优先级最高的四条
     * @return
     */
    @PostMapping("list")
    public Object list(){

        return  carouselService.list();
    }

}

