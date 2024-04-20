package com.coding.fullstack.auth.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SocialGiteeUser {
    /**
     * accessToken
     */
    @JsonProperty("access_token")
    private String accessToken;
    /**
     * tokenType
     */
    @JsonProperty("token_type")
    private String tokenType;
    /**
     * expiresIn
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;
    /**
     * refreshToken
     */
    @JsonProperty("refresh_token")
    private String refreshToken;
    /**
     * 权限范围
     */
    @JsonProperty("scope")
    private String scope;
    /**
     * createdAt
     */
    @JsonProperty("created_at")
    private Integer createdAt;

    private List<SocialGitterEmailInfo> emailInfos;

    private SocialGitterBasicInfo socialGitterBasicInfo;
}
