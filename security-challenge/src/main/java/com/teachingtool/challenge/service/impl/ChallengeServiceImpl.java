package com.teachingtool.challenge.service.impl;

import com.teachingtool.challenge.mapper.ChallengeMapper;
import com.teachingtool.challenge.mapper.ChallengeStatusMapper;
import com.teachingtool.param.ChallengeParam;
import com.teachingtool.pojo.Challenge;
import com.teachingtool.challenge.service.ChallengeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChallengeServiceImpl implements ChallengeService {

    @Autowired
    private ChallengeStatusMapper challengeStatusMapper;

    @Autowired
    private ChallengeMapper challengeMapper;

    @Override
    public List<Challenge> listChallenges(ChallengeParam challengeParam) {
        Integer userId = challengeParam.getUserId();
        List<Challenge> challenges = challengeStatusMapper.selectChallengesWithCompletion(userId);
        return challenges;
    }
    @Override
    public double calculateCompletionPercentage(ChallengeParam challengeParam) {
        Integer userId = challengeParam.getUserId();
        int totalChallenges = challengeMapper.countTotalChallenges();
        int completedChallenges = challengeStatusMapper.countCompletedChallengesByUserId(userId);
        if (totalChallenges == 0) return 0.0;
        return ((double) completedChallenges / totalChallenges) * 100;
    }

    @Override
    @Cacheable(value = "challenge",key = "#challengeID")
    public Object detail(Integer challengeID) {
        Challenge challenge = challengeMapper.selectDetailsById(challengeID);
        return challenge;
    }
}
