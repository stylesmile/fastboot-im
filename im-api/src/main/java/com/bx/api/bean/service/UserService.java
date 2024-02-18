package com.bx.api.bean.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bx.api.common.enums.ResultCode;
import com.bx.api.common.exception.GlobalException;
import com.bx.api.common.util.BeanUtils;
import com.bx.api.common.util.MD5Util;
import com.bx.api.config.JwtProperties;
import com.bx.api.domain.dto.LoginDTO;
import com.bx.api.domain.dto.ModifyPwdDTO;
import com.bx.api.domain.dto.RegisterDTO;
import com.bx.api.domain.dto.session.UserSession;
import com.bx.api.domain.entity.Friend;
import com.bx.api.domain.entity.GroupMember;
import com.bx.api.domain.entity.User;
import com.bx.api.domain.vo.LoginVO;
import com.bx.api.domain.vo.OnlineTerminalVO;
import com.bx.api.domain.vo.UserVO;
import com.bx.api.mapper.FriendMapper;
import com.bx.api.mapper.GroupMemberMapper;
import com.bx.api.mapper.UserMapper;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.util.IPUtil;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.server.Request;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
//public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
public class UserService {

    //    private final PasswordEncoder passwordEncoder;
    @AutoWired
    private FriendMapper friendMapper;
    @AutoWired
    private JwtProperties jwtProperties;
    @AutoWired
    private IMClient imClient;
    @AutoWired
    private UserMapper userMapper;

    @AutoWired
    private GroupMemberMapper groupMemberMapper;

    @AutoWired
    private GroupMemberService groupMemberService;
    @AutoWired
    private JedisTemplate jedisTemplate;
    @AutoWired
    private Jedis jedis;
    @AutoWired
    private SessionService sessionService;

    //@Override
    public LoginVO login(LoginDTO dto, Request request) {
        User user = this.findUserByUserName(dto.getUserName());
        if (null == user) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
        }
        if (!MD5Util.calculateMD5(dto.getPassword()).equals(user.getPassword())) {
//            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new GlobalException(ResultCode.PASSWOR_ERROR);
        }
        // 生成token
//        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        UserSession session = UserSession.builder()
                .nickName(user.getNickName())
                .userName(user.getUserName())
                .build();
        session.setTerminal(dto.getTerminal());
        session.setUserId(user.getId());
        String strJson = JSON.toJSONString(session);
        String accessToken = MD5Util.calculateMD5(strJson + System.currentTimeMillis() + IPUtil.getClientIP(request));
        jedisTemplate.setSerializeDataEx(
                String.format(IMRedisKey.TOKEN_USER_SESSION, accessToken),
                session, 600);

//        String accessToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getAccessTokenExpireIn(), jwtProperties.getAccessTokenSecret());
//        String refreshToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getRefreshTokenExpireIn(), jwtProperties.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(accessToken);
//        vo.setAccessToken(accessToken);
//        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(accessToken);
        vo.setAccessTokenExpiresIn(1800);
        vo.setRefreshTokenExpiresIn(604800);
//        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        //返回token
        // 设置缓存，600秒
        return vo;
    }

    //@Override
    public LoginVO refreshToken(String refreshToken, Request request) {
        UserSession session = jedisTemplate.getSerializeData(
                String.format(IMRedisKey.TOKEN_USER_SESSION, refreshToken), UserSession.class);
        String strJson = JSON.toJSONString(session);
        String accessToken = MD5Util.calculateMD5(strJson + System.currentTimeMillis() + IPUtil.getClientIP(request));
        jedisTemplate.setExpire(
                String.format(IMRedisKey.TOKEN_USER_SESSION, accessToken), 1800);

//        String accessToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getAccessTokenExpireIn(), jwtProperties.getAccessTokenSecret());
//        String refreshToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getRefreshTokenExpireIn(), jwtProperties.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(accessToken);
//        vo.setAccessToken(accessToken);
//        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(accessToken);
        vo.setAccessTokenExpiresIn(1800);
        vo.setRefreshTokenExpiresIn(604800);
//        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        //返回token
        // 设置缓存，600秒
        return vo;
        //验证 token
//        if (!JwtUtil.checkSign(refreshToken, jwtProperties.getRefreshTokenSecret())) {
//            throw new GlobalException("refreshToken无效或已过期");
//        }
//        String strJson = JwtUtil.getInfo(refreshToken);
//        Long userId = JwtUtil.getUserId(refreshToken);
//        String accessToken = JwtUtil.sign(userId, strJson, jwtProperties.getAccessTokenExpireIn(), jwtProperties.getAccessTokenSecret());
//        String newRefreshToken = JwtUtil.sign(userId, strJson, jwtProperties.getRefreshTokenExpireIn(), jwtProperties.getRefreshTokenSecret());
//        LoginVO vo = new LoginVO();
//        vo.setAccessToken(accessToken);
//        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
//        vo.setRefreshToken(newRefreshToken);
//        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
//        return vo;
    }

    //@Override
    public void register(RegisterDTO dto) {
        User user = this.findUserByUserName(dto.getUserName());
        if (null != user) {
            throw new GlobalException(ResultCode.USERNAME_ALREADY_REGISTER);
        }
        user = BeanUtils.copyProperties(dto, User.class);
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPassword(MD5Util.calculateMD5(user.getPassword()));
//        this.save(user);
        int result = userMapper.insert(user);
        log.info("注册用户，用户id:{},用户名:{},昵称:{}", user.getId(), dto.getUserName(), dto.getNickName());
    }

    //@Override
    public void modifyPassword(ModifyPwdDTO dto) {
        UserSession session = sessionService.getSession();
        ;
//        User user = this.getById(session.getUserId());
        User user = userMapper.selectById(session.getUserId());
        if (!MD5Util.calculateMD5(dto.getOldPassword()).equals(user.getPassword())) {
            throw new GlobalException("旧密码不正确");
        }
        user.setPassword(MD5Util.calculateMD5(dto.getNewPassword()));
//        this.updateById(user);
        userMapper.updateById(user);
        log.info("用户修改密码，用户id:{},用户名:{},昵称:{}", user.getId(), user.getUserName(), user.getNickName());
    }

    //@Override
    public User findUserByUserName(String username) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getUserName, username);
//        return this.getOne(queryWrapper);
        return userMapper.selectOne(queryWrapper);
    }

    //    @Transactional(rollbackFor = Exception.class)
    //@Override
    public void update(UserVO vo) {
        UserSession session = sessionService.getSession();
        ;
        if (!session.getUserId().equals(vo.getId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "不允许修改其他用户的信息!");
        }
//        User user = this.getById(vo.getId());
        User user = userMapper.selectById(vo.getId());
        if (Objects.isNull(user)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
        }
        // 更新好友昵称和头像
        if (!user.getNickName().equals(vo.getNickName()) || !user.getHeadImageThumb().equals(vo.getHeadImageThumb())) {
            QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Friend::getFriendId, session.getUserId());
//            List<Friend> friends = friendService.list(queryWrapper);
            List<Friend> friends = friendMapper.selectList(queryWrapper);
            for (Friend friend : friends) {
                friend.setFriendNickName(vo.getNickName());
                friend.setFriendHeadImage(vo.getHeadImageThumb());
                friendMapper.updateById(friend);
            }
//            friendService.updateBatchById(friends);
        }
        // 更新群聊中的头像
        if (!user.getHeadImageThumb().equals(vo.getHeadImageThumb())) {
            List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
            for (GroupMember member : members) {
                member.setHeadImage(vo.getHeadImageThumb());
            }
//            groupMemberService.updateBatchById(members);
            groupMemberMapper.selectBatchIds(members);
        }
        // 更新用户信息
        user.setNickName(vo.getNickName());
        user.setSex(vo.getSex());
        user.setSignature(vo.getSignature());
        user.setHeadImage(vo.getHeadImage());
        user.setHeadImageThumb(vo.getHeadImageThumb());
//        this.updateById(user);
        userMapper.updateById(user);
        log.info("用户信息更新，用户:{}}", user);
    }

    //@Override
    public UserVO findUserById(Long id) {
//        User user = this.getById(id);
        User user = userMapper.selectById(id);
        UserVO vo = BeanUtils.copyProperties(user, UserVO.class);
        vo.setOnline(imClient.isOnline(id));
        return vo;
    }

    //@Override
    public List<UserVO> findUserByName(String name) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(User::getUserName, name).or().like(User::getNickName, name).last("limit 20");
//        List<User> users = this.list(queryWrapper);
        List<User> users = userMapper.selectList(queryWrapper);
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        return users.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(onlineUserIds.contains(u.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    //@Override
    public List<OnlineTerminalVO> getOnlineTerminals(String userIds) {
        List<Long> userIdList = Arrays.stream(userIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        // 查询在线的终端
        Map<Long, List<IMTerminalType>> terminalMap = imClient.getOnlineTerminal(userIdList);
        // 组装vo
        List<OnlineTerminalVO> vos = new LinkedList<>();
        terminalMap.forEach((userId, types) -> {
            List<Integer> terminals = types.stream().map(IMTerminalType::code).collect(Collectors.toList());
            vos.add(new OnlineTerminalVO(userId, terminals));
        });
        return vos;
    }

    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
}
