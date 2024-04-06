package com.coding.fullstack.coupon.dao;

import com.coding.fullstack.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:22:40
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
