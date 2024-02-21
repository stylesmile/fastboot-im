package com.bx.imserver.service;

import cn.hutool.core.bean.BeanUtil;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.IMHeartbeatInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imserver.constant.ChannelAttrKey;
import com.google.gson.JsonObject;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.tool.JsonGsonUtil;
import io.github.stylesmile.websocket.WebsocketUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Slf4j
@Service
public class HeartbeatService {

    @AutoWired
    private JedisTemplate redisTemplate;

    public synchronized void process(JsonObject userSession, ChannelContext channelContext) {
        // 响应ws
        IMSendInfo sendInfo = new IMSendInfo();
        sendInfo.setCmd(IMCmdType.HEART_BEAT.code());
        WsResponse wsResponse = WsResponse.fromText(JsonGsonUtil.BeanToJson(sendInfo), StandardCharsets.UTF_8.toString());
        Tio.sendToUser(channelContext.tioConfig, userSession.get("userId").toString(), wsResponse);
        Long l = channelContext.getHeartbeatTimeout();
        Long userId = userSession.get("userId").getAsLong();
        WebsocketUtil.sendToUser(channelContext.tioConfig, userId.toString(), wsResponse);
//        ctx.channel().writeAndFlush(sendInfo);

        // 设置属性
//        AttributeKey<Long> heartBeatAttr = AttributeKey.valueOf(ChannelAttrKey.HEARTBEAT_TIMES);
//        Long heartbeatTimes = ctx.channel().attr(heartBeatAttr).get();
//        ctx.channel().attr(heartBeatAttr).set(++heartbeatTimes);
//        if (heartbeatTimes % 10 == 0) {
//            // 每心跳10次，用户在线状态续一次命
//            AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
//            Long userId = ctx.channel().attr(userIdAttr).get();
//            AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
//            Integer terminal = ctx.channel().attr(terminalAttr).get();
//            String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, userId.toString(), terminal.toString());
////            redisTemplate.expire(key, IMConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
//            redisTemplate.setExpire(key, (int) IMConstant.ONLINE_TIMEOUT_SECOND);
//        }
    }

}
