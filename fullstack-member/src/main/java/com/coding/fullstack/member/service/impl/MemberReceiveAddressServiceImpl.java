package com.coding.fullstack.member.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.member.dao.MemberReceiveAddressDao;
import com.coding.fullstack.member.entity.MemberReceiveAddressEntity;
import com.coding.fullstack.member.service.MemberReceiveAddressService;

@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity>
    implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(new Query<MemberReceiveAddressEntity>().getPage(params),
            new QueryWrapper<MemberReceiveAddressEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<MemberReceiveAddressEntity> getAddress(Long memberId) {
        List<MemberReceiveAddressEntity> list = this.list(Wrappers.lambdaQuery(MemberReceiveAddressEntity.class)
            .eq(MemberReceiveAddressEntity::getMemberId, memberId));
        return list;
    }
}