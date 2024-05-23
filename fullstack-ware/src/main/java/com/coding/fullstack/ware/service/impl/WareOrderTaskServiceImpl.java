package com.coding.fullstack.ware.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.ware.dao.WareOrderTaskDao;
import com.coding.fullstack.ware.entity.WareOrderTaskEntity;
import com.coding.fullstack.ware.service.WareOrderTaskService;

@Service("wareOrderTaskService")
public class WareOrderTaskServiceImpl extends ServiceImpl<WareOrderTaskDao, WareOrderTaskEntity>
    implements WareOrderTaskService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskEntity> page =
            this.page(new Query<WareOrderTaskEntity>().getPage(params), new QueryWrapper<WareOrderTaskEntity>());

        return new PageUtils(page);
    }

    @Override
    public WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
    }
}