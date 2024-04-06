package com.coding.fullstack.member.dao;

import com.coding.fullstack.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:45:16
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {
	
}
