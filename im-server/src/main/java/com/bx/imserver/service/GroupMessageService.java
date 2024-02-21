package com.bx.imserver.service;

import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imcommon.model.IMSendResult;
import com.bx.imcommon.model.IMUserInfo;
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
import java.util.List;

@Slf4j
@Service
public class GroupMessageService {
    @AutoWired
    private JedisTemplate redisTemplate;
    @AutoWired
    private Jedis jedis;

    public synchronized void process(IMRecvInfo recvInfo) {
        Integer senderId = 0;
        List<IMUserInfo> receivers = recvInfo.getReceivers();
        log.info("接收到群消息，发送者:{},接收用户数量:{}，内容:{}", senderId, receivers.size(), recvInfo.getData());
        for (IMUserInfo receiver : receivers) {
            try {
                ChannelContext channelCtx = UserChannelCtxMap.getChannelCtx2(receiver.getId(), receiver.getTerminal());
                if (channelCtx != null) {
                    // 推送消息到用户
                    IMSendInfo sendInfo = new IMSendInfo();
                    sendInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
                    sendInfo.setData(recvInfo.getData());
                    WsResponse wsResponse = WsResponse.fromText(JsonGsonUtil.BeanToJson(sendInfo), StandardCharsets.UTF_8.toString());
                    WebsocketUtil.sendToUser(channelCtx.tioConfig, receiver.getId().toString(), wsResponse);
//                channelCtx.channel().writeAndFlush(sendInfo);
                    // 消息发送成功确认
                    sendResult(recvInfo, receiver, IMSendCode.SUCCESS);

                } else {
                    // 不在线发送成功确认
                    sendResult(recvInfo, receiver, IMSendCode.NOT_FIND_CHANNEL);
                    log.error("未找到channel,发送者:{},接收id:{}，内容:{}", senderId, receiver.getId(), recvInfo.getData());
                }
            } catch (Exception e) {
                // 消息发送失败确认
                sendResult(recvInfo, receiver, IMSendCode.UNKONW_ERROR);
                log.error("发送消息异常,发送者:{},接收id:{}，内容:{}", senderId, receiver.getId(), recvInfo.getData());
            }
        }
    }


    private void sendResult(IMRecvInfo recvInfo, IMUserInfo receiver, IMSendCode sendCode) {
        if (recvInfo.getSendResult()) {
            IMSendResult result = new IMSendResult();
            result.setSender(recvInfo.getSender());
            result.setReceiver(receiver);
            result.setCode(sendCode.code());
            result.setData(recvInfo.getData());
            // 推送到结果队列
            String key = StrUtil.join(":", IMRedisKey.IM_RESULT_GROUP_QUEUE, recvInfo.getServiceName());
//            redisTemplate.opsForList().rightPush(key, result);
            jedis.rpush(GsonByteUtils.toByteArray(key), GsonByteUtils.toByteArray(result));

        }
    }
}
