package com.teachingtool;

import com.teachingtool.clients.ProductClient;
import com.teachingtool.clients.WebSocketClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(basePackages = "com.teachingtool.mapper")
@SpringBootApplication
@EnableFeignClients(clients = {ProductClient.class, WebSocketClient.class})
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class,args);
    }
}
