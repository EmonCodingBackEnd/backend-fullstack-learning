package com.coding.fullstack.ware.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.common.utils.R;
import com.coding.fullstack.ware.dao.WareInfoDao;
import com.coding.fullstack.ware.entity.WareInfoEntity;
import com.coding.fullstack.ware.feign.MemberFeignService;
import com.coding.fullstack.ware.service.WareInfoService;
import com.coding.fullstack.ware.vo.FareVo;
import com.coding.fullstack.ware.vo.MemberAddressVo;

import lombok.RequiredArgsConstructor;

@Service("wareInfoService")
@RequiredArgsConstructor
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    private final MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String)params.get("key");

        LambdaQueryWrapper<WareInfoEntity> lambdaQuery = Wrappers.lambdaQuery(WareInfoEntity.class);

        if (StringUtils.isNotEmpty(key)) {
            lambdaQuery.and(i -> i.eq(WareInfoEntity::getId, key).or().like(WareInfoEntity::getName, key).or()
                .like(WareInfoEntity::getAddress, key));
        }

        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), lambdaQuery);

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R info = memberFeignService.info(addrId);
        if (info.getCode() == 0) {
            MemberAddressVo address = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});
            if (address != null) {
                String fee =
                    address.getPhone() != null ? address.getPhone().substring(address.getPhone().length() - 2) : "0";
                fareVo.setAddress(address);
                fareVo.setFare(new BigDecimal(fee));
                return fareVo;
            }
        }
        return null;
    }
}