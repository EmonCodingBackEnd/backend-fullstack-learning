package com.coding.fullstack.seckill.service;

import java.util.List;

import com.coding.fullstack.seckill.to.SecKillSkuRedisTo;

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * 
     * @return
     */
    List<SecKillSkuRedisTo> getCurrentSeckillSkus();

    /**
     * 获取某一个SKU商品的秒杀预告信息
     * 
     * @param skuId
     * @return
     */
    SecKillSkuRedisTo getCurrentSeckillInfo(Long skuId);

    /**
     * 
     * @param killId
     * @param key
     * @param num
     * @return
     */
    String kill(String killId, String key, Integer num);
}
