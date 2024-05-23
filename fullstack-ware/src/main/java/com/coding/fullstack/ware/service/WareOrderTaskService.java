package com.coding.fullstack.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 18:06:44
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn);
}

