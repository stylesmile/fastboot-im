package com.bx.imserver.websocket.service;

import cn.hutool.core.bean.BeanUtil;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.IMLoginInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imserver.constant.ChannelAttrKey;
import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.UserChannelCtxMap;
import com.google.gson.JsonObject;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.ioc.Value;
import io.github.stylesmile.jedis.JedisTemplate;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
@Service
public class LoginProcessor {

    @AutoWired
    private JedisTemplate redisTemplate;

    @Value("${jwt.accessToken.secret}")
    private String accessTokenSecret;
    @Value("websocket.port")

    private String accessTokenSecret2;

    public synchronized void process(ChannelHandlerContext ctx, IMLoginInfo loginInfo) {
        String token = loginInfo.getAccessToken();
        JsonObject userSession = redisTemplate.getSerializeData(
                String.format(IMRedisKey.TOKEN_USER_SESSION, token),
                JsonObject.class);
//        if (!JwtUtil.checkSign(loginInfo.getAccessToken(), accessTokenSecret)) {
        if (userSession == null) {
            ctx.channel().close();
            log.warn("用户token校验不通过，强制下线,token:{}", loginInfo.getAccessToken());
        }
//        String strInfo = JwtUtil.getInfo(loginInfo.getAccessToken());
//        String strInfo = JwtUtil.getInfo(loginInfo.getAccessToken());
//        IMSessionInfo sessionInfo = JSON.parseObject(strInfo, IMSessionInfo.class);
//        IMSessionInfo sessionInfo = new IMSessionInfo();
//        Long userId = sessionInfo.getUserId();
//        Integer terminal = sessionInfo.getTerminal();
        Long userId = userSession.get("userId").getAsLong();
        Integer terminal = userSession.get("terminal").getAsInt();
        log.info("用户登录，userId:{}", userId);
        ChannelHandlerContext context = UserChannelCtxMap.getChannelCtx(userId, terminal);
        if (context != null && !ctx.channel().id().equals(context.channel().id())) {
            // 不允许多地登录,强制下线
            IMSendInfo<Object> sendInfo = new IMSendInfo<>();
            sendInfo.setCmd(IMCmdType.FORCE_LOGUT.code());
            sendInfo.setData("您已在其他地方登陆，将被强制下线");
            context.channel().writeAndFlush(sendInfo);
            log.info("异地登录，强制下线,userId:{}", userId);
        }
        // 绑定用户和channel
        UserChannelCtxMap.addChannelCtx(userId, terminal, ctx);
        // 设置用户id属性
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
        ctx.channel().attr(userIdAttr).set(userId);
        // 设置用户终端类型
        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
        ctx.channel().attr(terminalAttr).set(terminal);
        // 初始化心跳次数
        AttributeKey<Long> heartBeatAttr = AttributeKey.valueOf("HEARTBEAt_TIMES");
        ctx.channel().attr(heartBeatAttr).set(0L);
        // 在redis上记录每个user的channelId，15秒没有心跳，则自动过期
        String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, userId.toString(), terminal.toString());
//        redisTemplate.opsForValue().set(key, IMServerGroup.serverId, IMConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        redisTemplate.setSerializeDataEx(key, IMServerGroup.serverId, (int) IMConstant.ONLINE_TIMEOUT_SECOND);
        // 响应ws
        IMSendInfo<Object> sendInfo = new IMSendInfo<>();
        sendInfo.setCmd(IMCmdType.LOGIN.code());
        ctx.channel().writeAndFlush(sendInfo);
    }


    public IMLoginInfo transForm(Object o) {
        HashMap map = (HashMap) o;
        return BeanUtil.fillBeanWithMap(map, new IMLoginInfo(), false);
    }
}
