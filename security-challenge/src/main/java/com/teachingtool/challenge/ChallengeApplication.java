package com.teachingtool.challenge;

import com.teachingtool.clients.ChallengeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {ChallengeClient.class})
public class ChallengeApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChallengeApplication.class, args);
    }
}
