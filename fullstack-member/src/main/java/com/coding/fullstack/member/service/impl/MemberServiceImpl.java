package com.coding.fullstack.member.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.common.utils.R;
import com.coding.fullstack.member.dao.MemberDao;
import com.coding.fullstack.member.dao.MemberLevelDao;
import com.coding.fullstack.member.entity.MemberEntity;
import com.coding.fullstack.member.entity.MemberLevelEntity;
import com.coding.fullstack.member.service.MemberService;
import com.coding.fullstack.member.vo.*;

import lombok.RequiredArgsConstructor;

@Service("memberService")
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    private final MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page =
            this.page(new Query<MemberEntity>().getPage(params), new QueryWrapper<MemberEntity>());

        return new PageUtils(page);
    }

    @Override
    public R regist(UserRegistVo registVo) {
        MemberEntity memberEntity = new MemberEntity();
        // 设置默认会员等级
        MemberLevelEntity defaultLevel = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(defaultLevel.getId());
        // 检查用户名和手机号是否唯一
        boolean phoneExists = checkPhoneUnique(registVo.getPhone());
        if (phoneExists) {
            return R.error("手机号不唯一！");
        }
        boolean usernameExists = checkUsernameUnique(registVo.getUsername());
        if (usernameExists) {
            return R.error("用户名不唯一！");
        }

        memberEntity.setMobile(registVo.getPhone());
        memberEntity.setUsername(registVo.getUsername());
        memberEntity.setNickname(registVo.getUsername());

        // 秘钥要进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(registVo.getPassword());
        memberEntity.setPassword(password);
        int insert = this.baseMapper.insert(memberEntity);
        return insert > 0 ? R.ok() : R.error("保存会员失败");
    }

    @Override
    public boolean checkPhoneUnique(String phone) {
        return this.baseMapper
            .selectCount(Wrappers.lambdaQuery(MemberEntity.class).eq(MemberEntity::getMobile, phone)) > 0;
    }

    @Override
    public boolean checkUsernameUnique(String username) {
        return this.baseMapper.exists(Wrappers.lambdaQuery(MemberEntity.class).eq(MemberEntity::getUsername, username));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public R login(UserLoginVo loginVo) {
        String loginacct = loginVo.getLoginacct();
        String password = loginVo.getPassword();

        MemberEntity memberEntity = this.baseMapper.selectOne(Wrappers.lambdaQuery(MemberEntity.class)
            .eq(MemberEntity::getUsername, loginacct).or().eq(MemberEntity::getMobile, loginacct));
        if (memberEntity == null) {
            return R.error("用户名或密码错误！");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, memberEntity.getPassword())) {
            return R.error("用户名或密码错误！");
        }
        return R.ok().setData(memberEntity);
    }

    @Override
    public R login(SocialGiteeUser loginVo) {
        List<SocialGitterEmailInfo> emailInfos = loginVo.getEmailInfos();
        String email = emailInfos.get(0).getEmail();
        SocialGitterBasicInfo socialGitterBasicInfo = loginVo.getSocialGitterBasicInfo();
        String name = socialGitterBasicInfo.getName();
        String login = socialGitterBasicInfo.getLogin();
        Integer id = socialGitterBasicInfo.getId();
        String avatarUrl = socialGitterBasicInfo.getAvatarUrl();
        // 当前社交用户是否已经注册过
        MemberEntity memberEntity = this.baseMapper.selectOne(Wrappers.lambdaQuery(MemberEntity.class)
            .eq(MemberEntity::getSocialUid, id.toString()).eq(MemberEntity::getSocialType, "gitee"));
        if (memberEntity != null) {
            MemberEntity member = new MemberEntity();
            member.setId(memberEntity.getId());
            member.setNickname(name);
            member.setEmail(email);
            member.setHeader(avatarUrl);
            this.baseMapper.updateById(member);
            memberEntity = this.baseMapper.selectById(memberEntity.getId());
        } else {
            MemberEntity member = new MemberEntity();
            // 设置默认会员等级
            MemberLevelEntity defaultLevel = memberLevelDao.getDefaultLevel();
            member.setLevelId(defaultLevel.getId());
            member.setNickname(name);
            member.setEmail(email);
            member.setHeader(avatarUrl);
            member.setSocialUid(id.toString());
            member.setSocialType("gitee");
            this.baseMapper.insert(member);
            memberEntity = this.baseMapper.selectById(member.getId());
        }
        return R.ok().setData(memberEntity);
    }
}