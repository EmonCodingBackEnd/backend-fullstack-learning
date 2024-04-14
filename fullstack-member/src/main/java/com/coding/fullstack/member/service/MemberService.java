package com.coding.fullstack.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.R;
import com.coding.fullstack.member.entity.MemberEntity;
import com.coding.fullstack.member.vo.UserLoginVo;
import com.coding.fullstack.member.vo.UserRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:45:16
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    R regist(UserRegistVo registVo);

    boolean checkPhoneUnique(String phone);
    boolean checkUsernameUnique(String username);

    R login(UserLoginVo loginVo);
}

