package com.coding.fullstack.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.member.entity.MemberLevelEntity;

import java.util.Map;

/**
 * 会员等级
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:45:16
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

