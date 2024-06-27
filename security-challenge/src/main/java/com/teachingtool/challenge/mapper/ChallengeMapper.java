package com.teachingtool.challenge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teachingtool.pojo.Challenge;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChallengeMapper extends BaseMapper<Challenge> {
    int countTotalChallenges();

    Challenge selectDetailsById(Integer challengeID);
}
