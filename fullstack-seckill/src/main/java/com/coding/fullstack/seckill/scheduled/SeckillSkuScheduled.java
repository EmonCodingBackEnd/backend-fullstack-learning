package com.coding.fullstack.seckill.scheduled;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coding.fullstack.seckill.service.SeckillService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @formatter:off
/**
 * 秒杀商品的定时上架
 *
 * 每天晚上3点；上架最近三天需要秒杀的商品。
 * 当天 00:00:00 - 23:59:59
 * 明天 00:00:00 - 23:59:59
 * 后天 00:00:00 - 23:59:59
 */
// @formatter:on
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillSkuScheduled {
    public static final String UPLOAD_LOCK = "seckill:upload:lock";
    private final SeckillService seckillService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        log.info("上架秒杀的商品！");
        // 1、重复上架无需处理
        // 分布式所
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        try {
            lock.lock(10, TimeUnit.SECONDS);
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }
}
