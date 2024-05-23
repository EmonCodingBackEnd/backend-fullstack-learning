package com.coding.fullstack.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.ware.entity.WareOrderTaskDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 库存工作单
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 18:06:44
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<WareOrderTaskDetailEntity> getOrderTaskDetailByTaskId(Long id);
}

