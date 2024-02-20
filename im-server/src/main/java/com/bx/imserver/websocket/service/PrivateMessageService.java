package com.bx.imserver.websocket.service;

import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imcommon.model.IMSendResult;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imserver.netty.UserChannelCtxMap;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.tool.GsonByteUtils;
import io.github.stylesmile.tool.JsonGsonUtil;
import io.github.stylesmile.websocket.WebsocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.ChannelContext;
import org.tio.websocket.common.WsResponse;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class PrivateMessageService {

    @AutoWired
    private JedisTemplate redisTemplate;
    @AutoWired
    private Jedis jedis;

    //    public void process(IMRecvInfo recvInfo) {
//    public synchronized void process(JsonObject jsonObject, ChannelContext channelContext) {
    public void process(IMRecvInfo recvInfo) {
        IMUserInfo sender = recvInfo.getSender();
        IMUserInfo receiver = recvInfo.getReceivers().get(0);
        log.info("接收到消息，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(), recvInfo.getData());
        try {
//            ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
            ChannelContext channelCtx = UserChannelCtxMap.getChannelCtx2(receiver.getId(), receiver.getTerminal());
            if (channelCtx != null) {
                // 推送消息到用户
                IMSendInfo<Object> sendInfo = new IMSendInfo<>();
                sendInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                sendInfo.setData(recvInfo.getData());
                WsResponse wsResponse = WsResponse.fromText(JsonGsonUtil.BeanToJson(sendInfo), StandardCharsets.UTF_8.toString());

                WebsocketUtil.sendToUser(channelCtx.tioConfig, receiver.getId().toString(), wsResponse);
//                channelCtx.channel().writeAndFlush(sendInfo);
                // 消息发送成功确认
                sendResult(recvInfo, IMSendCode.SUCCESS);
            } else {
                // 消息推送失败确认
                sendResult(recvInfo, IMSendCode.NOT_FIND_CHANNEL);
                log.error("未找到channel，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(), recvInfo.getData());
            }
        } catch (Exception e) {
            // 消息推送失败确认
            sendResult(recvInfo, IMSendCode.UNKONW_ERROR);
            log.error("发送异常，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(), recvInfo.getData(), e);
        }

    }

    private void sendResult(IMRecvInfo recvInfo, IMSendCode sendCode) {
        if (recvInfo.getSendResult()) {
            IMSendResult<Object> result = new IMSendResult<>();
            result.setSender(recvInfo.getSender());
            result.setReceiver(recvInfo.getReceivers().get(0));
            result.setCode(sendCode.code());
            result.setData(recvInfo.getData());
            // 推送到结果队列
            String key = StrUtil.join(":", IMRedisKey.IM_RESULT_PRIVATE_QUEUE, recvInfo.getServiceName());
            jedis.rpush(GsonByteUtils.toByteArray(key), GsonByteUtils.toByteArray(recvInfo));
        }
    }
}
