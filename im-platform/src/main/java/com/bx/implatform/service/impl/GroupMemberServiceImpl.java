package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.mapper.GroupMemberMapper;
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
public class GroupMemberServiceImpl {
    @AutoWired
    GroupMemberMapper groupMemberMapper;

    @CacheEvict(key = "#member.getGroupId()")
//    @Override
    public boolean save(GroupMember member) {
        return groupMemberMapper.insert(member) > 0;
    }

    @CacheEvict(key = "#groupId")
//    @Override
    public boolean saveOrUpdateBatch(Long groupId, List<GroupMember> members) {
        for (GroupMember m : members) {
            groupMemberMapper.updateById(m);

        }
//        return this.saveOrUpdateBatch(members);
        return true;
    }


    //    @Override
    public GroupMember findByGroupAndUserId(Long groupId, Long userId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        return groupMemberMapper.selectOne(wrapper);
    }

    //    @Override
    public List<GroupMember> findByUserId(Long userId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getQuit, false);
        return groupMemberMapper.selectList(memberWrapper);
    }

    //    @Override
    public List<GroupMember> findByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId);
//        return this.list(memberWrapper);
        return groupMemberMapper.selectList(memberWrapper);
    }

    @Cacheable(key = "#groupId")
//    @Override
    public List<Long> findUserIdsByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getQuit, false);
//        List<GroupMember> members = this.list(memberWrapper);
        List<GroupMember> members = groupMemberMapper.selectList(memberWrapper);
        return members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
    }

    @CacheEvict(key = "#groupId")
//    @Override
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
