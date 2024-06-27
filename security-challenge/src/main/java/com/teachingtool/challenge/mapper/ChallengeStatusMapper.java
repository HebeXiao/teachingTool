package com.teachingtool.challenge.mapper;

import com.teachingtool.pojo.Challenge;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChallengeStatusMapper {
    int countCompletedChallengesByUserId(Integer userId);

    List<Challenge> selectChallengesWithCompletion(Integer userId);
}

