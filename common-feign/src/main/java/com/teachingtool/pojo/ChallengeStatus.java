package com.teachingtool.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@TableName("challenge_status")
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeStatus {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("challenge_id")
    private Integer challengeId;
    @JsonProperty("is_completed")
    private boolean isCompleted;
}
