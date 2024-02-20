//package com.bx.imserver.netty.processor;
//
//import cn.hutool.core.bean.BeanUtil;
//import com.bx.imcommon.contant.IMConstant;
//import com.bx.imcommon.contant.IMRedisKey;
//import com.bx.imcommon.enums.IMCmdType;
//import com.bx.imcommon.model.IMHeartbeatInfo;
//import com.bx.imcommon.model.IMSendInfo;
//import com.bx.imserver.constant.ChannelAttrKey;
//import io.github.stylesmile.annotation.AutoWired;
//import io.github.stylesmile.annotation.Service;
//import io.github.stylesmile.jedis.JedisTemplate;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.util.AttributeKey;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.HashMap;
//
//@Slf4j
//@Service
//public class HeartbeatProcessor extends AbstractMessageProcessor<IMHeartbeatInfo> {
////public class HeartbeatProcessor {
//
//    @AutoWired
//    private JedisTemplate redisTemplate;
//
//    @Override
//    public void process(ChannelHandlerContext ctx, IMHeartbeatInfo beatInfo) {
//        // 响应ws
//        IMSendInfo sendInfo = new IMSendInfo();
//        sendInfo.setCmd(IMCmdType.HEART_BEAT.code());
//        ctx.channel().writeAndFlush(sendInfo);
//
//        // 设置属性
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
//    }
//
//    @Override
//    public IMHeartbeatInfo transForm(Object o) {
//        HashMap map = (HashMap) o;
//        return BeanUtil.fillBeanWithMap(map, new IMHeartbeatInfo(), false);
//    }
//}
