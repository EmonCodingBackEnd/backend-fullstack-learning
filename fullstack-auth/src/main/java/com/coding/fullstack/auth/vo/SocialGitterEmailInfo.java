package com.coding.fullstack.auth.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
