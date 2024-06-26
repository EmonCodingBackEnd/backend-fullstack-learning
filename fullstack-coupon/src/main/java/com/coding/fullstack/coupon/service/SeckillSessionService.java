package com.coding.fullstack.coupon.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.coupon.entity.SeckillSessionEntity;

/**
 * 秒杀活动场次
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:22:40
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SeckillSessionEntity> getLatest3DaySession();
}
