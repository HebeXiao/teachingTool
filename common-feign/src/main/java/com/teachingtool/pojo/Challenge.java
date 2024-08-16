package com.teachingtool.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@TableName("challenges")
@AllArgsConstructor
@NoArgsConstructor
public class Challenge implements Serializable {
    public static final Long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @JsonProperty("challenge_id")
    private Integer challengeId;
    @JsonProperty("challenge_name")
    private String challengeName;
    @JsonProperty("challenge_description")
    private String challengeDescription;
    @JsonProperty("challenge_type")
    private String challengeType;
    @JsonProperty("challenge_details")
    private String challengeDetails;
    private boolean isCompleted;

}