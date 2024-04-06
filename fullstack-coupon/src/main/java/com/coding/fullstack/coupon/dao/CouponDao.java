package com.coding.fullstack.coupon.dao;

import com.coding.fullstack.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:22:41
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
