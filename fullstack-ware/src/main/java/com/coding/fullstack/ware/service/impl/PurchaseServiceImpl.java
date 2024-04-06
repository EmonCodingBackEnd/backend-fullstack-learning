package com.coding.fullstack.ware.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.constant.WareConstant;
import com.coding.common.exception.BizCodeEnum;
import com.coding.common.exception.RRException;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.ware.dao.PurchaseDao;
import com.coding.fullstack.ware.entity.PurchaseDetailEntity;
import com.coding.fullstack.ware.entity.PurchaseEntity;
import com.coding.fullstack.ware.service.PurchaseDetailService;
import com.coding.fullstack.ware.service.PurchaseService;
import com.coding.fullstack.ware.service.WareSkuService;
import com.coding.fullstack.ware.vo.MergeVo;
import com.coding.fullstack.ware.vo.PurchaseDoneVo;
import com.coding.fullstack.ware.vo.PurchaseItemDoneVo;

import lombok.RequiredArgsConstructor;

@Service("purchaseService")
@RequiredArgsConstructor
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    private final PurchaseDetailService purchaseDetailService;
    private final WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page =
            this.page(new Query<PurchaseEntity>().getPage(params), new QueryWrapper<PurchaseEntity>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params),
            Wrappers.lambdaQuery(PurchaseEntity.class)
                .eq(PurchaseEntity::getStatus, WareConstant.PurchaseStatusEnum.CREATED.getCode()).or()
                .eq(PurchaseEntity::getStatus, WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()));

        List<PurchaseEntity> records = page.getRecords();
        records.forEach(purchase -> {
            if (WareConstant.PurchaseStatusEnum.CREATED.getCode() == purchase.getStatus()) {
                purchase.setAssigneeName("--");
                purchase.setPhone("--");
            }
        });

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        } else {
            PurchaseEntity purchaseEntity = this.getById(purchaseId);
            if (!(WareConstant.PurchaseStatusEnum.CREATED.getCode() == purchaseEntity.getStatus())
                && !(WareConstant.PurchaseStatusEnum.ASSIGNED.getCode() == purchaseEntity.getStatus())) {
                throw new RRException("不是新建状态，不能合并", BizCodeEnum.UNKNOW_EXCEPTION.getCode());
            }
        }

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(purchaseDetailService::getById)
            .filter(item -> item != null && (WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() == item.getStatus()
                || WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode() == item.getStatus()))
            .map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(item.getId());
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setStatus(WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
        if (collect.size() == 0) {
            throw new RRException("没有需要合并的采购项", BizCodeEnum.UNKNOW_EXCEPTION.getCode());
        }

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void receive(List<Long> purchaseIds) {
        // 1、确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = purchaseIds.stream().map(this::getById).filter(
            purchase -> purchase != null && (WareConstant.PurchaseStatusEnum.CREATED.getCode() == purchase.getStatus()
                || WareConstant.PurchaseStatusEnum.ASSIGNED.getCode() == purchase.getStatus()))
            .peek(purchase -> {
                purchase.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
                purchase.setUpdateTime(new Date());
            }).collect(Collectors.toList());
        if (collect.size() == 0) {
            return;
        }
        // 2、改变采购单的状态
        this.updateBatchById(collect);
        // 3、改变采购项的状态
        collect.forEach(purchase -> {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.list(Wrappers
                .lambdaQuery(PurchaseDetailEntity.class).eq(PurchaseDetailEntity::getPurchaseId, purchase.getId()));
            List<PurchaseDetailEntity> purchaseDetails = purchaseDetailEntities.stream().map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(item.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetails);
        });
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void done(PurchaseDoneVo doneVo) {
        // 1、改变采购单状态
        boolean flag = doneVo.getItems().stream()
            .anyMatch(item -> item.getStatus() != WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
        this.update(
            Wrappers.lambdaUpdate(PurchaseEntity.class)
                .set(PurchaseEntity::getStatus,
                    flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode()
                        : WareConstant.PurchaseStatusEnum.HASERROR.getCode())
                .eq(PurchaseEntity::getId, doneVo.getId()));
        // 2、改变采购项的状态
        List<PurchaseDetailEntity> detailEntities = new ArrayList<>();
        for (PurchaseItemDoneVo item : doneVo.getItems()) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item.getItemId());
            detailEntity.setStatus(item.getStatus());
            detailEntity.setReason(item.getReason());
            detailEntities.add(detailEntity);
            // 3、将成功采购的进行入库
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode()) {
                PurchaseDetailEntity detail = purchaseDetailService.getById(detailEntity.getId());
                Long wareId = detail.getWareId();
                Long skuId = detail.getSkuId();
                Integer skuNum = detail.getSkuNum();
                BigDecimal skuPrice = detail.getSkuPrice();

                wareSkuService.addStock(skuId, wareId, skuNum);
            }
        }
        purchaseDetailService.updateBatchById(detailEntities);
    }
}