package com.bx.api.bean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bx.api.common.contant.RedisKey;
import com.bx.api.domain.entity.Friend;
import com.bx.api.domain.entity.User;
import com.bx.api.common.enums.ResultCode;
import com.bx.api.common.exception.GlobalException;
import com.bx.api.domain.vo.FriendVO;
import com.bx.api.mapper.FriendMapper;
import com.bx.api.mapper.UserMapper;
import com.bx.api.domain.dto.session.UserSession;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;

@Slf4j
@Service
@CacheConfig(cacheNames = RedisKey.IM_CACHE_FRIEND)
public class FriendService {

    @AutoWired
    private UserMapper userMapper;

    @AutoWired
    private FriendMapper friendMapper;

    @AutoWired
    private SessionService sessionService;

    //    @Override
    public List<Friend> findFriendByUserId(Long userId) {
        LambdaQueryWrapper<Friend> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Friend::getUserId, userId);
        return friendMapper.selectList(queryWrapper);
    }


    //    @Transactional(rollbackFor = Exception.class)
//    @Override
    public void addFriend(Long friendId) {
        long userId = sessionService.getSession().getUserId();
        if (userId == friendId) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "不允许添加自己为好友");
        }
        // 互相绑定好友关系
//        FriendService proxy = (FriendService) AopContext.currentProxy();
//        proxy.bindFriend(userId, friendId);
//        proxy.bindFriend(friendId, userId);
        this.bindFriend(userId, friendId);
        this.bindFriend(friendId, userId);
        log.info("添加好友，用户id:{},好友id:{}", userId, friendId);
    }


    //    @Transactional(rollbackFor = Exception.class)
//    @Override
    public void delFriend(Long friendId) {
        long userId = sessionService.getSession().getUserId();
        // 互相解除好友关系，走代理清理缓存
        FriendService proxy = (FriendService) AopContext.currentProxy();
        proxy.unbindFriend(userId, friendId);
        proxy.unbindFriend(friendId, userId);
        log.info("删除好友，用户id:{},好友id:{}", userId, friendId);
    }


    //    @Cacheable(key = "#userId1+':'+#userId2")
//    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId1)
                .eq(Friend::getFriendId, userId2);
        return friendMapper.selectCount(queryWrapper) > 0;
    }


    //    @Override
    public void update(FriendVO vo) {
        long userId = sessionService.getSession().getUserId();
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, vo.getId());

        Friend f = friendMapper.selectOne(queryWrapper);
        if (f == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "对方不是您的好友");
        }

        f.setFriendHeadImage(vo.getHeadImage());
        f.setFriendNickName(vo.getNickName());
        this.friendMapper.updateById(f);
    }


    /**
     * 单向绑定好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
    @CacheEvict(key = "#userId+':'+#friendId")
    public void bindFriend(Long userId, Long friendId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        if (friendMapper.selectCount(queryWrapper) == 0) {
            Friend friend = new Friend();
            friend.setUserId(userId);
            friend.setFriendId(friendId);
            User friendInfo = userMapper.selectById(friendId);
            friend.setFriendHeadImage(friendInfo.getHeadImage());
            friend.setFriendNickName(friendInfo.getNickName());
            friendMapper.insert(friend);
        }
    }


    /**
     * 单向解除好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
//    @CacheEvict(key = "#userId+':'+#friendId")
    public void unbindFriend(Long userId, Long friendId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
//        List<Friend> friends = this.list(queryWrapper);
        List<Friend> friends = friendMapper.selectList(queryWrapper);
//        friends.forEach(friend -> this.removeById(friend.getId()));
        friends.forEach(friend -> friendMapper.deleteById(friend.getId()));
    }


    //    @Override
    public FriendVO findFriend(Long friendId) {
        UserSession session = sessionService.getSession();
        long userId = session.getUserId();
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Friend::getUserId, session.getUserId())
                .eq(Friend::getFriendId, friendId);
//        Friend friend = this.getOne(wrapper);
        Friend friend = friendMapper.selectOne(wrapper);
        if (friend == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "对方不是您的好友");
        }
        FriendVO vo = new FriendVO();
        vo.setId(friend.getFriendId());
        vo.setHeadImage(friend.getFriendHeadImage());
        vo.setNickName(friend.getFriendNickName());
        return vo;
    }
}
