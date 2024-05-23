package com.coding.fullstack.ware.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.to.mq.OrderTo;
import com.coding.common.to.mq.StockLockedTo;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.ware.entity.WareSkuEntity;
import com.coding.fullstack.ware.vo.SkuHasStockVo;
import com.coding.fullstack.ware.vo.WareSkuLockVo;

/**
 * 商品库存
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 18:06:44
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     * 
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    boolean unLockStock(StockLockedTo to);

    /**
     * 防止订单服务卡顿，导致订单状态消息一直改变不了。最终库存消息先到期，查询订单是新建状态，忽略库存解锁。
     * @param to
     * @return
     */
    void unLockStock(OrderTo to);
}
