package com.teachingtool.challenge.controller;

import com.teachingtool.param.ChallengeParam;
import com.teachingtool.pojo.Challenge;
import com.teachingtool.challenge.service.ChallengeService;
import com.teachingtool.pojo.ChallengeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("challenge")
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;
    @PostMapping("list")
    public ResponseEntity<List<Challenge>> listChallenges(@RequestBody ChallengeParam challengeParam) {
        List<Challenge> challenges = challengeService.listChallenges(challengeParam);
        return ResponseEntity.ok(challenges); // Return status code 200 and challenge list
    }

    @PostMapping("progress")
    public double getCompletionPercentage(@RequestBody ChallengeParam challengeParam) {
        return challengeService.calculateCompletionPercentage(challengeParam);
    }

    @PostMapping("detail")
    public Object detail(@RequestBody Map<String,Integer> param){
        Integer challengeID = param.get("challengeID");
        return challengeService.detail(challengeID);
    }

    @PostMapping("add")
    public ResponseEntity<?> addChallengeStatus(@RequestBody ChallengeStatus challengeStatus) {
        try {
            challengeService.addChallengeStatus(challengeStatus);
            return ResponseEntity.ok("Challenge status added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding challenge status");
        }
    }
}
