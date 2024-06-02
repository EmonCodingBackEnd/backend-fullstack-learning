package com.coding.fullstack.seckill.service.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.coding.common.to.mq.SeckilklOrderTo;
import com.coding.common.utils.R;
import com.coding.common.vo.MemberEntityVo;
import com.coding.fullstack.seckill.feign.CouponFeignService;
import com.coding.fullstack.seckill.feign.ProductFeignService;
import com.coding.fullstack.seckill.interceptor.LoginUserInterceptor;
import com.coding.fullstack.seckill.service.SeckillService;
import com.coding.fullstack.seckill.to.SecKillSkuRedisTo;
import com.coding.fullstack.seckill.vo.SeckillSessionsWithSkus;
import com.coding.fullstack.seckill.vo.SkuInfoVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final CouponFeignService couponFeignService;
    private final ProductFeignService productFeignService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    // 活动
    public static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    // 商品
    public static final String SKUS_CACHE_PREFIX = "seckill:skus:";
    // 库存
    public static final String STOCK_CACHE_PREFIX = "seckill:stock:"; // + 随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 1、扫描最近三天需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            List<SeckillSessionsWithSkus> sessionData =
                r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {});
            // 1、缓存活动信息
            saveSessionInfos(sessionData);
            // 2、缓存活动的关联商品信息
            saveSessionSkuInfos(sessionData);
        }
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessionData) {
        sessionData.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            List<String> collect;
            if (session.getRelationSkus() != null && session.getRelationSkus().size() > 0) {
                collect = session.getRelationSkus().stream().map(e -> session.getId() + "_" + e.getSkuId().toString())
                    .collect(Collectors.toList());
                Boolean hasKey = stringRedisTemplate.hasKey(key);
                if (Boolean.FALSE.equals(hasKey)) {
                    stringRedisTemplate.opsForList().leftPushAll(key, collect);
                }
            }
        });

    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessionData) {
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
        sessionData.stream().forEach(session -> {
            session.getRelationSkus().stream().forEach(skuVo -> {
                String key = session.getId() + "_" + skuVo.getSkuId().toString();
                if (Boolean.FALSE.equals(ops.hasKey(key))) {
                    SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();
                    // 缓存商品
                    // 1、Sku的基本数据
                    R info = productFeignService.info(skuVo.getSkuId());
                    if (info.getCode() == 0) {
                        SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                        secKillSkuRedisTo.setSkuInfo(skuInfo);
                    }

                    // BeanUtils
                    // 2、Sku的秒杀信息
                    BeanUtils.copyProperties(skuVo, secKillSkuRedisTo);

                    // 3、设置上当前商品的秒杀时间信息
                    secKillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    secKillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    // 4、设置商品的随机码 seckill?skuId=1&key=daladsik
                    String randomCode = UUID.randomUUID().toString().replace("-", "");
                    secKillSkuRedisTo.setRandomCode(randomCode);

                    // 5、分布式信号量：限流
                    RSemaphore semaphore = redissonClient.getSemaphore(STOCK_CACHE_PREFIX + randomCode);
                    // 商品可以秒杀的数量作为秒杀信号量
                    semaphore.trySetPermits(skuVo.getSeckillCount().intValue());

                    String sto = JSON.toJSONString(secKillSkuRedisTo);
                    ops.put(key, sto);
                }
            });
        });
    }

    public List<SecKillSkuRedisTo> blockHandler(BlockException e) {
        log.warn("blockHandler=>资源getCurrentSeckillSkus已被限流！{}", e.getMessage());
        return null;
    }

    public List<SecKillSkuRedisTo> fallback(Throwable e) {
        log.warn("fallback=>资源getCurrentSeckillSkus已被限流！{}", e.getMessage());
        return null;
    }

    /**
     * 注意 blockHandler 函数会在原方法被限流/降级/系统保护的时候调用，而 fallback 函数会针对所有类型的异常
     */
    @SentinelResource(value = "getCurrentSeckillSkus", blockHandler = "blockHandler", fallback = "fallback")
    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {

        // 1、确定当前时间属于哪一个秒杀场次
        long time = System.currentTimeMillis();

        try (Entry entry = SphU.entry("seckillSkus")) {
            Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                // key=场次_商品
                for (String key : keys) {
                    String[] s = key.replace(SESSIONS_CACHE_PREFIX, "").split("_");
                    long start = Long.parseLong(s[0]);
                    long end = Long.parseLong(s[1]);
                    if (time >= start && time <= end) {
                        // 2、获取这个秒杀场次的所有商品 比如：range=[2_1,2_2]
                        List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                        if (range != null && !range.isEmpty()) {
                            BoundHashOperations<String, String, String> ops =
                                stringRedisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
                            List<String> list = ops.multiGet(range);
                            if (list != null) {
                                return list.stream().map(e -> {
                                    SecKillSkuRedisTo redisTo = JSON.parseObject(e, SecKillSkuRedisTo.class);
                                    // 当前秒杀开始就需要随机码
                                    redisTo.setRandomCode(null);
                                    return redisTo;
                                }).collect(Collectors.toList());
                            }
                            break;
                        }
                    }
                }
            }
        } catch (BlockException e) {
            log.warn("资源seckillSkus已被限流！{}", e.getMessage());
        }
        return null;
    }

    @Override
    public SecKillSkuRedisTo getCurrentSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys != null && keys.size() > 0) {
            String regex = "\\d+_" + skuId;
            // 6_1
            for (String key : keys) {
                if (Pattern.matches(regex, key)) {
                    String s = ops.get(key);
                    SecKillSkuRedisTo to = JSON.parseObject(s, SecKillSkuRedisTo.class);
                    if (to != null) {
                        Long startTime = to.getStartTime();
                        Long endTime = to.getEndTime();
                        long time = System.currentTimeMillis();
                        if (!(time >= startTime && time <= endTime)) {
                            to.setRandomCode("");
                        }
                        return to;
                    }
                }
            }
        }
        return null;
    }

    // TODO: 2024/6/2 上架秒杀商品时，每一个数据都有过期时间。
    // TODO: 2024/6/2 秒杀后续的流程，简化了收货地址等
    @Override
    public String kill(String killId, String key, Integer num) {
        MemberEntityVo memberEntityVo = LoginUserInterceptor.threadLocal.get();
        Long userId = memberEntityVo.getId();

        // 1、获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
        String json = ops.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        SecKillSkuRedisTo to = JSON.parseObject(json, SecKillSkuRedisTo.class);
        // 校验时间的合法性
        Long startTime = to.getStartTime();
        Long endTime = to.getEndTime();
        long now = System.currentTimeMillis();
        if (now < startTime || now > endTime) {
            return null;
        }

        // 校验随机码的合法性
        String randomCode = to.getRandomCode();
        String pss = String.format("%s_%s", to.getPromotionSessionId(), to.getSkuId());
        if (!key.equals(randomCode) || !pss.equals(killId)) {
            return null;
        }

        // 3、验证抢购数量：允许一次购买多个，但不允许多次购买多个
        if (num > (to.getSeckillLimit().intValue())) {
            return null;
        }

        // 4、验证这个人是否已经购买过。幂等性；如果只要秒杀成功，就去占位。userId_sessionId_skuId
        String redisKey = String.format("%s_%s_%s", userId, to.getPromotionSessionId(), to.getSkuId());
        long ttl = endTime - now;
        // 数量：允许一次购买多个，但不允许多次购买多个
        Boolean aBoolean =
            stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
        if (Boolean.FALSE.equals(aBoolean)) {
            return null;
        }

        RSemaphore semaphore = redissonClient.getSemaphore(STOCK_CACHE_PREFIX + randomCode);
        try {
            boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
            // 快速下单，发送MQ消息
            if (b) {
                String timeId = IdWorker.getTimeId();
                SeckilklOrderTo orderTo = new SeckilklOrderTo();
                orderTo.setOrderSn(timeId);
                orderTo.setPromotionSessionId(to.getPromotionSessionId());
                orderTo.setSkuId(to.getSkuId());
                orderTo.setSeckillPrice(to.getSeckillPrice());
                orderTo.setNum(num);
                orderTo.setMemberId(memberEntityVo.getId());

                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo,
                    new CorrelationData(UUID.randomUUID().toString()));
                return timeId;
            }
        } catch (InterruptedException e) {
            return null;
        }
        return null;
    }
}
