package com.teachingtool.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teachingtool.pojo.Address;
import lombok.Data;

@Data
public class AddressParam {
    @JsonProperty("user_id")
    private Integer userId;
    private Address add;
}
