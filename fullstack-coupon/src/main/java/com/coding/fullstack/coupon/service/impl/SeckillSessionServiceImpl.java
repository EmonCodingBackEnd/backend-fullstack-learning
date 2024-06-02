package com.coding.fullstack.coupon.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.coupon.dao.SeckillSessionDao;
import com.coding.fullstack.coupon.entity.SeckillSessionEntity;
import com.coding.fullstack.coupon.entity.SeckillSkuRelationEntity;
import com.coding.fullstack.coupon.service.SeckillSessionService;
import com.coding.fullstack.coupon.service.SeckillSkuRelationService;

import lombok.RequiredArgsConstructor;

@Service("seckillSessionService")
@RequiredArgsConstructor
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity>
    implements SeckillSessionService {

    private final SeckillSkuRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page =
            this.page(new Query<SeckillSessionEntity>().getPage(params), new QueryWrapper<SeckillSessionEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {
        // 计算最近3天
        LambdaQueryWrapper<SeckillSessionEntity> lambda = Wrappers.lambdaQuery(SeckillSessionEntity.class)
            .between(SeckillSessionEntity::getStartTime, startTime(), endTime());
        List<SeckillSessionEntity> list = this.list(lambda);
        if (list != null && list.size() > 0) {
            return list.stream().peek(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> relationEntities =
                    relationService.list(Wrappers.lambdaQuery(SeckillSkuRelationEntity.class)
                        .eq(SeckillSkuRelationEntity::getPromotionSessionId, id));
                session.setRelationSkus(relationEntities);
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(now.plusDays(2), max);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}