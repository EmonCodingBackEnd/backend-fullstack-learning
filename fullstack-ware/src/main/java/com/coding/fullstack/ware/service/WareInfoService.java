package com.coding.fullstack.ware.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.ware.entity.WareInfoEntity;
import com.coding.fullstack.ware.vo.FareVo;

/**
 * 仓库信息
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 18:06:44
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据用户的收货地址计算运费
     * 
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}
