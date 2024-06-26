package com.coding.fullstack.ware.dao;

import com.coding.fullstack.ware.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 18:06:44
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
	
}
