package com.teachingtool.challenge.service;

import com.teachingtool.param.ChallengeParam;
import com.teachingtool.pojo.Challenge;
import com.teachingtool.pojo.ChallengeStatus;

import java.util.List;

public interface ChallengeService {
    List<Challenge> listChallenges(ChallengeParam challengeParam);

    double calculateCompletionPercentage(ChallengeParam challengeParam);

    Object detail(Integer challengeID);

    void addChallengeStatus(ChallengeStatus challengeStatus);
}