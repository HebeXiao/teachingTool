package com.teachingtool.user;

import com.teachingtool.clients.ProductClient;
import com.teachingtool.clients.WebSocketClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(basePackages = "com.teachingtool.user.mapper")
@SpringBootApplication
//开启feign的客户端,暂时不需要
@EnableFeignClients(clients = {WebSocketClient.class})
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
    }

}
