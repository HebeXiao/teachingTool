package com.teachingtool.challenge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teachingtool.pojo.Challenge;
import com.teachingtool.pojo.ChallengeStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChallengeStatusMapper extends BaseMapper<ChallengeStatus> {
    int countCompletedChallengesByUserId(Integer userId);

    List<Challenge> selectChallengesWithCompletion(Integer userId);
}

