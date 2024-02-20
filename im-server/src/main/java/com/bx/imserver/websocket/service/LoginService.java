package com.bx.imserver.websocket.service;

import cn.hutool.core.bean.BeanUtil;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.IMLoginInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.UserChannelCtxMap;
import com.google.gson.JsonObject;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.tool.JsonGsonUtil;
import io.github.stylesmile.websocket.WebsocketUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Slf4j
@Service
public class LoginService {

    @AutoWired
    private JedisTemplate redisTemplate;

    public synchronized void process(JsonObject userSession, ChannelContext channelContext) {

//        if (!JwtUtil.checkSign(loginInfo.getAccessToken(), accessTokenSecret)) {
        if (userSession == null) {
            Tio.remove(channelContext, "receive close flag");
//            log.debug("用户token校验不通过，强制下线,token:{}", token);
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
        UserChannelCtxMap.addChannelCtx2(userId, terminal,channelContext);
//        ChannelHandlerContext context = UserChannelCtxMap.getChannelCtx(userId, terminal);
//        if (channelContext != null && !ctx.channel().id().equals(context.channel().id())) {
//            // 不允许多地登录,强制下线
//            IMSendInfo<Object> sendInfo = new IMSendInfo<>();
//            sendInfo.setCmd(IMCmdType.FORCE_LOGUT.code());
//            sendInfo.setData("您已在其他地方登陆，将被强制下线");
//            context.channel().writeAndFlush(sendInfo);
//            log.info("异地登录，强制下线,userId:{}", userId);
//        }

//        // 绑定用户和channel
        Tio.bindUser(channelContext, userId.toString());
//        UserChannelCtxMap.addChannelCtx(userId, terminal, ctx);
//        // 设置用户id属性
//        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
//        ctx.channel().attr(userIdAttr).set(userId);
//        // 设置用户终端类型
//        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
//        ctx.channel().attr(terminalAttr).set(terminal);
//        // 初始化心跳次数
//        AttributeKey<Long> heartBeatAttr = AttributeKey.valueOf("HEARTBEAt_TIMES");
//        ctx.channel().attr(heartBeatAttr).set(0L);
//        // 在redis上记录每个user的channelId，15秒没有心跳，则自动过期
        String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, userId.toString(), terminal.toString());
//        redisTemplate.opsForValue().set(key, IMServerGroup.serverId, IMConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        redisTemplate.setSerializeDataEx(key, IMServerGroup.serverId, (int) IMConstant.ONLINE_TIMEOUT_SECOND);
        // 响应ws
        IMSendInfo<Object> sendInfo = new IMSendInfo<>();
        sendInfo.setCmd(IMCmdType.LOGIN.code());
        WsResponse wsResponse = WsResponse.fromText(JsonGsonUtil.BeanToJson(sendInfo), StandardCharsets.UTF_8.toString());
        WebsocketUtil.sendToUser(channelContext.tioConfig, userId.toString(), wsResponse);
    }


    public IMLoginInfo transForm(Object o) {
        HashMap map = (HashMap) o;
        return BeanUtil.fillBeanWithMap(map, new IMLoginInfo(), false);
    }
}
