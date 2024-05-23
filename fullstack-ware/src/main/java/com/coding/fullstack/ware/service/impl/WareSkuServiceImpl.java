package com.coding.fullstack.ware.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.exception.NoStockException;
import com.coding.common.to.mq.OrderTo;
import com.coding.common.to.mq.StockLockedDetailTo;
import com.coding.common.to.mq.StockLockedTo;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.common.utils.R;
import com.coding.fullstack.ware.dao.WareSkuDao;
import com.coding.fullstack.ware.entity.WareOrderTaskDetailEntity;
import com.coding.fullstack.ware.entity.WareOrderTaskEntity;
import com.coding.fullstack.ware.entity.WareSkuEntity;
import com.coding.fullstack.ware.feign.OrderFeignService;
import com.coding.fullstack.ware.feign.ProductFeignService;
import com.coding.fullstack.ware.service.WareOrderTaskDetailService;
import com.coding.fullstack.ware.service.WareOrderTaskService;
import com.coding.fullstack.ware.service.WareSkuService;
import com.coding.fullstack.ware.vo.OrderItemVo;
import com.coding.fullstack.ware.vo.OrderVo;
import com.coding.fullstack.ware.vo.SkuHasStockVo;
import com.coding.fullstack.ware.vo.WareSkuLockVo;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service("wareSkuService")
@RequiredArgsConstructor
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    private final ProductFeignService productFeignService;
    private final OrderFeignService orderFeignService;

    private final RabbitTemplate rabbitTemplate;
    private final WareOrderTaskService orderTaskService;
    private final WareOrderTaskDetailService orderTaskDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String skuId = (String)params.get("skuId");
        String wareId = (String)params.get("wareId");

        LambdaQueryWrapper<WareSkuEntity> lambdaQuery = Wrappers.lambdaQuery(WareSkuEntity.class);
        if (skuId != null && !skuId.isEmpty()) {
            lambdaQuery.eq(WareSkuEntity::getSkuId, skuId);
        }
        if (wareId != null && !wareId.isEmpty()) {
            lambdaQuery.eq(WareSkuEntity::getWareId, wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), lambdaQuery);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断如果还没有库存记录，就新增
        WareSkuEntity wareSkuEntity = this.baseMapper.selectOne(Wrappers.lambdaQuery(WareSkuEntity.class)
            .eq(WareSkuEntity::getSkuId, skuId).eq(WareSkuEntity::getWareId, wareId));
        if (wareSkuEntity != null) {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        } else {
            WareSkuEntity wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setStock(skuNum);
            wareSku.setWareId(wareId);
            wareSku.setStockLocked(0);
            // 远程查询并设置sku的名字，如果失败，整个事务无需回滚！
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map<String, Object>)info.get("skuInfo");
                    wareSku.setSkuName((String)skuInfo.get("skuName"));
                }
            } catch (Exception e) {
                log.error("远程查询并设置sku的名字异常", e);
            }
            this.baseMapper.insert(wareSku);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return new ArrayList<>();
        }

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            Long count = this.baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count != null && count > 0);
            return skuHasStockVo;

        }).collect(Collectors.toList());
        return collect;
    }

    // @formatter:off

    /**
     * 为某个订单锁定库存
     * <p>
     * 库存解锁的场景：
     * 1）、下单成功，订单过期没有支付被系统自动取消、被用户手动取消。都需要解锁库存
     * 2）、下单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     * 之前锁定的库存，需要自动解锁。
     *
     * @param vo
     * @return
     */
    // @formatter:on
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /*
        保存库存工作单的详情。
        方便追溯。
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);

        // 1、按照下单的收货地址，找到一个就近仓库，锁定库存
        // 2、找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> stocks = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // 查询这个商品在哪里有库存
            List<Long> wareIds = this.baseMapper.listWareIdHasStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 2、锁定库存
        for (SkuWareHasStock stock : stocks) {
            boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            // 1、如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给MQ
            // 2、锁定失败。前面保存的工作单信息也就回滚了，发送出去的消息，即使要解锁记录，也找不到id，就不用解锁了。
            for (Long wareId : wareIds) {
                // 成功就返回1，否则返回0
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, stock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    // TODO: 2024/5/17 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
                    taskDetailEntity.setSkuId(skuId);
                    taskDetailEntity.setSkuName("");
                    taskDetailEntity.setSkuNum(stock.getNum());
                    taskDetailEntity.setTaskId(taskEntity.getId());
                    taskDetailEntity.setWareId(wareId);
                    taskDetailEntity.setLockStatus(1);
                    orderTaskDetailService.save(taskDetailEntity);

                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockLockedDetailTo stockLockedDetailTo = new StockLockedDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, stockLockedDetailTo);
                    stockLockedTo.setDetail(stockLockedDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo,
                        new CorrelationData(UUID.randomUUID().toString()));
                    break;
                    // 锁定成功
                    // LockStockResult result = new LockStockResult();
                    // result.setSkuId(skuId);
                    // result.setNum(stock.getNum());
                    // result.setWareId(wareId);
                    // result.setLocked(true);
                    // return result;
                } else {
                    // 当前仓库锁失败，重试下一个仓库
                }
            }

            // 当前商品的所有仓库都没有锁住
            if (!skuStocked) {
                throw new NoStockException(skuId);
            }
        }

        // 3、肯定全部都是锁定成功的
        return true;
    }

    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

    @Transactional
    @Override
    public boolean unLockStock(StockLockedTo to) {
        Long id = to.getId(); // 库存工作单id
        StockLockedDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        /*
        解锁
        1、查询数据库关于这个订单的锁定库存信息。
            有：库存锁定成功了
                若没有这个订单，必须解锁。【需解锁】
                若有这个订单：
                    订单已取消，解锁库存【需解锁】
                    订单正常，不需要解锁库存
            没有：库存锁定失败了，库存回滚了，这种情况无需解锁。
         */
        WareOrderTaskDetailEntity taskDetail = orderTaskDetailService.getById(detailId);
        if (taskDetail != null) {
            // 解锁
            WareOrderTaskEntity task = orderTaskService.getById(id);
            String orderSn = task.getOrderSn(); // 根据订单号查询订单的状态
            R r = orderFeignService.getOrderByOrderSn(orderSn);
            if (r.getCode() == 0) {
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {});
                // 订单不存在，或者订单已经被取消了，才能解锁库存
                if (orderVo == null || orderVo.getStatus() == 4) {
                    // 当前库存工作单详情，已锁定但未解锁，才可以解锁
                    if (taskDetail.getLockStatus() == 1) {
                        return unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                // 查询订单失败了，重新入队。
                throw new RuntimeException("远程服务失败");
            }
        } else {
            // 无需解锁
        }
        return true;
    }

    private boolean unLockStock(Long skuId, Long wareId, Integer num, Long detailId) {
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity taskDetail = new WareOrderTaskDetailEntity();
        taskDetail.setId(detailId);
        taskDetail.setLockStatus(2); // 变为已解锁
        orderTaskDetailService.updateById(taskDetail);
        // 库存解锁
        return this.baseMapper.unlockStock(skuId, wareId, num, detailId) > 0;
    }

    @Transactional
    @Override
    public void unLockStock(OrderTo to) {
        String orderSn = to.getOrderSn(); // 根据订单号查询订单的状态
        // 查一下最新库存的状态，防止重复解锁库存
        WareOrderTaskEntity taskEntity = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = taskEntity.getId();
        // 按照工作单找到所有没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> detailEntities = orderTaskDetailService.getOrderTaskDetailByTaskId(id);
        for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
            unLockStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum(),
                detailEntity.getId());
        }
    }
}