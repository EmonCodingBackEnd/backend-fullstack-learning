package com.coding.common.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 会员
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:45:16
 */
@Data
public class MemberEntityVo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;
	/**
	 * 会员等级id
	 */
	private Long levelId;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 昵称
	 */
	private String nickname;
	/**
	 * 手机号码
	 */
	private String mobile;
	/**
	 * 邮箱
	 */
	private String email;
	/**
	 * 头像
	 */
	private String header;
	/**
	 * 性别
	 */
	private Integer gender;
	/**
	 * 生日
	 */
	private Date birth;
	/**
	 * 所在城市
	 */
	private String city;
	/**
	 * 职业
	 */
	private String job;
	/**
	 * 个性签名
	 */
	private String sign;
	/**
	 * 用户来源
	 */
	private Integer sourceType;
	/**
	 * 积分
	 */
	private Integer integration;
	/**
	 * 成长值
	 */
	private Integer growth;
	/**
	 * 启用状态
	 */
	private Integer status;
	/**
	 * 注册时间
	 */
	private Date createTime;

	/**
	 * 社交账号ID
	 */
	private String socialUid;

	/**
	 * 社交账号类型[gitee,weibo,weixin,qq]
	 */
	private String socialType;

}
