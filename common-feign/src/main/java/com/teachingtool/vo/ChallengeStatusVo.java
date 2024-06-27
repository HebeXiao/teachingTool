package com.teachingtool.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ChallengeStatusVo {
    @JsonProperty("challenge_id")
    private Long challengeId;

    @JsonProperty("challenge_name")
    private String challengeName;

    @JsonProperty("challenge_description")
    private String challengeDescription;

    @JsonProperty("challenge_type")
    private String challengeType;

    @JsonProperty("is_completed")
    private boolean isCompleted;
}

