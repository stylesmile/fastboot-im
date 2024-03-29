package com.bx.api.bean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bx.api.common.contant.RedisKey;
import com.bx.api.domain.entity.GroupMember;
import com.bx.api.mapper.GroupMemberMapper;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP_MEMBER_ID)
//public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements IGroupMemberService {
public class GroupMemberService {
    @AutoWired
    GroupMemberMapper groupMemberMapper;

    @CacheEvict(key = "#member.getGroupId()")
    public boolean save(GroupMember member) {
        return groupMemberMapper.insert(member) > 0;
    }

    @CacheEvict(key = "#groupId")
    public boolean saveOrUpdateBatch(Long groupId, List<GroupMember> members) {
        for (GroupMember m : members) {
            groupMemberMapper.insert(m);
        }
        return true;
    }

    public GroupMember findByGroupAndUserId(Long groupId, Long userId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        return groupMemberMapper.selectOne(wrapper);
    }

    public List<GroupMember> findByUserId(Long userId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getQuit, false);
        return groupMemberMapper.selectList(memberWrapper);
    }

    public List<GroupMember> findByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId);
//        return this.list(memberWrapper);
        return groupMemberMapper.selectList(memberWrapper);
    }

    @Cacheable(key = "#groupId")
    public List<Long> findUserIdsByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getQuit, false);
//        List<GroupMember> members = this.list(memberWrapper);
        List<GroupMember> members = groupMemberMapper.selectList(memberWrapper);
        return members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
    }

    @CacheEvict(key = "#groupId")
    public void removeByGroupId(Long groupId) {
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .set(GroupMember::getQuit, true);
//        this.update(wrapper);
        groupMemberMapper.update(wrapper);
    }

    @CacheEvict(key = "#groupId")
//    @Override
    public void removeByGroupAndUserId(Long groupId, Long userId) {
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .set(GroupMember::getQuit, true);
//        this.update(wrapper);

        groupMemberMapper.update(wrapper);
    }
}
