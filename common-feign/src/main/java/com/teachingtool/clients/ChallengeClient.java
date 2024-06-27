package com.teachingtool.clients;

import com.teachingtool.pojo.Challenge;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "challenge-service", url = "http://challenge-service")
public interface ChallengeClient {

    @GetMapping("/challenges")
    List<Challenge> getAllChallenges();
}
