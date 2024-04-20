package com.coding.fullstack.member.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SocialGitterEmailInfo {
    /**
     * email
     */
    @JsonProperty("email")
    private String email;
    /**
     * state
     */
    @JsonProperty("state")
    private String state;
    /**
     * scope
     */
    @JsonProperty("scope")
    private List<String> scope;
}
