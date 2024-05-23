package com.coding.fullstack.ware.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.ware.dao.WareOrderTaskDetailDao;
import com.coding.fullstack.ware.entity.WareOrderTaskDetailEntity;
import com.coding.fullstack.ware.service.WareOrderTaskDetailService;

@Service("wareOrderTaskDetailService")
public class WareOrderTaskDetailServiceImpl extends ServiceImpl<WareOrderTaskDetailDao, WareOrderTaskDetailEntity>
    implements WareOrderTaskDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskDetailEntity> page = this.page(new Query<WareOrderTaskDetailEntity>().getPage(params),
            new QueryWrapper<WareOrderTaskDetailEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<WareOrderTaskDetailEntity> getOrderTaskDetailByTaskId(Long id) {
        return this.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status", 1));
    }
}