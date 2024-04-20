package com.coding.fullstack.member.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SocialGitterBasicInfo {
    /**
     * id
     */
    @JsonProperty("id")
    private Integer id;
    /**
     * login
     */
    @JsonProperty("login")
    private String login;
    /**
     * name
     */
    @JsonProperty("name")
    private String name;
    /**
     * avatarUrl
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;
    /**
     * url
     */
    @JsonProperty("url")
    private String url;
    /**
     * htmlUrl
     */
    @JsonProperty("html_url")
    private String htmlUrl;
    /**
     * remark
     */
    @JsonProperty("remark")
    private String remark;
    /**
     * followersUrl
     */
    @JsonProperty("followers_url")
    private String followersUrl;
    /**
     * followingUrl
     */
    @JsonProperty("following_url")
    private String followingUrl;
    /**
     * gistsUrl
     */
    @JsonProperty("gists_url")
    private String gistsUrl;
    /**
     * starredUrl
     */
    @JsonProperty("starred_url")
    private String starredUrl;
    /**
     * subscriptionsUrl
     */
    @JsonProperty("subscriptions_url")
    private String subscriptionsUrl;
    /**
     * organizationsUrl
     */
    @JsonProperty("organizations_url")
    private String organizationsUrl;
    /**
     * reposUrl
     */
    @JsonProperty("repos_url")
    private String reposUrl;
    /**
     * eventsUrl
     */
    @JsonProperty("events_url")
    private String eventsUrl;
    /**
     * receivedEventsUrl
     */
    @JsonProperty("received_events_url")
    private String receivedEventsUrl;
    /**
     * type
     */
    @JsonProperty("type")
    private String type;
    /**
     * blog
     */
    @JsonProperty("blog")
    private Object blog;
    /**
     * weibo
     */
    @JsonProperty("weibo")
    private Object weibo;
    /**
     * bio
     */
    @JsonProperty("bio")
    private Object bio;
    /**
     * publicRepos
     */
    @JsonProperty("public_repos")
    private Integer publicRepos;
    /**
     * publicGists
     */
    @JsonProperty("public_gists")
    private Integer publicGists;
    /**
     * followers
     */
    @JsonProperty("followers")
    private Integer followers;
    /**
     * following
     */
    @JsonProperty("following")
    private Integer following;
    /**
     * stared
     */
    @JsonProperty("stared")
    private Integer stared;
    /**
     * watched
     */
    @JsonProperty("watched")
    private Integer watched;
    /**
     * createdAt
     */
    @JsonProperty("created_at")
    private String createdAt;
    /**
     * updatedAt
     */
    @JsonProperty("updated_at")
    private String updatedAt;
    /**
     * email
     */
    @JsonProperty("email")
    private Object email;
}
