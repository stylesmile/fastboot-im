package com.bx.imclient.sender;

import cn.hutool.core.collection.CollUtil;
import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.model.*;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.ioc.Value;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.tool.GsonByteUtils;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class IMSender {

    //    @Resource(name="IMRedisTemplate")
//    private RedisTemplate<String, Object> redisTemplate;
    @AutoWired
    JedisTemplate jedisTemplate;
    @AutoWired
    Jedis jedis;

    @Value("fast.name")
    private String appName;
    @AutoWired
    private MessageListenerMulticaster listenerMulticaster;

    public <T> void sendPrivateMessage(IMPrivateMessage<T> message) {
        List<IMSendResult> results = new LinkedList<>();
        for (Integer terminal : message.getRecvTerminals()) {
            // 获取对方连接的channelId
            String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, message.getRecvId().toString(), terminal.toString());
//            Integer serverId = (Integer)redisTemplate.opsForValue().get(key);
            Integer serverId = jedisTemplate.getSerializeData(key, Integer.class);
            // 如果对方在线，将数据存储至redis，等待拉取推送
            if (serverId != null) {
                String sendKey = String.join(":", IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, serverId.toString());
                IMRecvInfo recvInfo = new IMRecvInfo();
                recvInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                recvInfo.setSendResult(message.getSendResult());
                recvInfo.setServiceName(appName);
                recvInfo.setSender(message.getSender());
                recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getRecvId(), terminal)));
                recvInfo.setData(message.getData());
                jedis.rpush(GsonByteUtils.toByteArray(sendKey), GsonByteUtils.toByteArray(recvInfo));
            } else {
                IMSendResult result = new IMSendResult();
                result.setSender(message.getSender());
                result.setReceiver(new IMUserInfo(message.getRecvId(), terminal));
                result.setCode(IMSendCode.NOT_ONLINE.code());
                result.setData(message.getData());
                results.add(result);
            }
        }
        // 推送给自己的其他终端
        if (message.getSendToSelf()) {
            for (Integer terminal : IMTerminalType.codes()) {
                if (message.getSender().getTerminal().equals(terminal)) {
                    continue;
                }
                // 获取终端连接的channelId
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, message.getSender().getId().toString(), terminal.toString());
//                Integer serverId = (Integer) redisTemplate.opsForValue().get(key);
                Integer serverId = jedisTemplate.getSerializeData(key, Integer.class);
                // 如果终端在线，将数据存储至redis，等待拉取推送
                if (serverId != null) {
                    String sendKey = String.join(":", IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, serverId.toString());
                    IMRecvInfo recvInfo = new IMRecvInfo();
                    // 自己的消息不需要回推消息结果
                    recvInfo.setSendResult(false);
                    recvInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                    recvInfo.setSender(message.getSender());
                    recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getSender().getId(), terminal)));
                    recvInfo.setData(message.getData());
//                    jedisTemplate.rpushSerializeData(sendKey, recvInfo);
                    jedis.rpush(GsonByteUtils.toByteArray(sendKey), GsonByteUtils.toByteArray(recvInfo));

                }
            }
        }
        // 对离线用户回复消息状态
        if (message.getSendResult() && !results.isEmpty()) {
            listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, results);
        }
    }

    public <T> void sendGroupMessage(IMGroupMessage<T> message) {

        // 根据群聊每个成员所连的IM-server，进行分组
        Map<String, IMUserInfo> sendMap = new HashMap<>();
        for (Integer terminal : message.getRecvTerminals()) {
            message.getRecvIds().forEach(id -> {
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, id.toString(), terminal.toString());
                sendMap.put(key, new IMUserInfo(id, terminal));
            });
        }

        // 批量拉取
        List<Integer> serverIds = new ArrayList<>();
        for (String s : sendMap.keySet()) {
            Integer o = jedisTemplate.getSerializeData(s, Integer.class);
            if (o != null) {
                serverIds.add(o);
            }
        }
        // 格式:map<服务器id,list<接收方>>
        Map<Integer, List<IMUserInfo>> serverMap = new HashMap<>();
        List<IMUserInfo> offLineUsers = new LinkedList<>();
        int idx = 0;
        for (Map.Entry<String, IMUserInfo> entry : sendMap.entrySet()) {
            if (idx >= serverIds.size()) {
                continue;
            }
            Integer serverId = serverIds.get(idx++);

            if (serverId != null) {
                List<IMUserInfo> list = serverMap.computeIfAbsent(Integer.valueOf(serverId.toString()), o -> new LinkedList<>());
                list.add(entry.getValue());
            } else {
                // 加入离线列表
                offLineUsers.add(entry.getValue());
            }
        }
        // 逐个server发送
        for (Map.Entry<Integer, List<IMUserInfo>> entry : serverMap.entrySet()) {
            IMRecvInfo recvInfo = new IMRecvInfo();
            recvInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
            recvInfo.setReceivers(new LinkedList<>(entry.getValue()));
            recvInfo.setSender(message.getSender());
            recvInfo.setServiceName(appName);
            recvInfo.setSendResult(message.getSendResult());
            recvInfo.setData(message.getData());
            // 推送至队列
            String key = String.join(":", IMRedisKey.IM_MESSAGE_GROUP_QUEUE, entry.getKey().toString());
//            redisTemplate.opsForList().rightPush(key, recvInfo);
            jedis.rpush(GsonByteUtils.toByteArray(key), GsonByteUtils.toByteArray(recvInfo));


        }

        // 推送给自己的其他终端
        if (message.getSendToSelf()) {
            for (Integer terminal : IMTerminalType.codes()) {
                if (terminal.equals(message.getSender().getTerminal())) {
                    continue;
                }
                // 获取终端连接的channelId
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, message.getSender().getId().toString(), terminal.toString());
//                Integer serverId = (Integer) redisTemplate.opsForValue().get(key);
                Integer serverId = jedisTemplate.getSerializeData(key, Integer.class);

                // 如果终端在线，将数据存储至redis，等待拉取推送
                if (serverId != null) {
                    IMRecvInfo recvInfo = new IMRecvInfo();
                    recvInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
                    recvInfo.setSender(message.getSender());
                    recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getSender().getId(), terminal)));
                    // 自己的消息不需要回推消息结果
                    recvInfo.setSendResult(false);
                    recvInfo.setData(message.getData());
                    String sendKey = String.join(":", IMRedisKey.IM_MESSAGE_GROUP_QUEUE, serverId.toString());
//                    jedisTemplate.rpushSerializeData(sendKey, recvInfo);
                    jedis.rpush(GsonByteUtils.toByteArray(sendKey), GsonByteUtils.toByteArray(recvInfo));

//                    jedisTemplate.rpush(sendKey, JsonGsonUtil.BeanToJson(recvInfo));
                }
            }
        }
        // 对离线用户回复消息状态
        if (message.getSendResult() && !offLineUsers.isEmpty()) {
            List<IMSendResult> results = new LinkedList<>();
            for (IMUserInfo offLineUser : offLineUsers) {
                IMSendResult result = new IMSendResult();
                result.setSender(message.getSender());
                result.setReceiver(offLineUser);
                result.setCode(IMSendCode.NOT_ONLINE.code());
                result.setData(message.getData());
                results.add(result);
            }
            listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE, results);
        }

    }

    public Map<Long, List<IMTerminalType>> getOnlineTerminal(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        // 把所有用户的key都存起来
        Map<String, IMUserInfo> userMap = new HashMap<>();
        for (Long id : userIds) {
            for (Integer terminal : IMTerminalType.codes()) {
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, id.toString(), terminal.toString());
                userMap.put(key, new IMUserInfo(id, terminal));
            }
        }
        // 批量拉取
//        List<Object> serverIds = redisTemplate.opsForValue().multiGet(userMap.keySet());
        List<Object> serverIds = new ArrayList<>();
        for (String s : userMap.keySet()) {
            Object o = jedisTemplate.getSerializeData(s, Object.class);
            serverIds.add(o);
        }
        int idx = 0;
        Map<Long, List<IMTerminalType>> onlineMap = new HashMap<>();
        for (Map.Entry<String, IMUserInfo> entry : userMap.entrySet()) {
            // serverid有值表示用户在线
            if (serverIds.get(idx++) != null) {
                IMUserInfo userInfo = entry.getValue();
                List<IMTerminalType> terminals = onlineMap.computeIfAbsent(userInfo.getId(), o -> new LinkedList<>());
                terminals.add(IMTerminalType.fromCode(userInfo.getTerminal()));
            }
        }
        // 去重并返回
        return onlineMap;
    }

    public Boolean isOnline(Long userId) {
        String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, userId.toString(), "*");
        return !Objects.requireNonNull(jedisTemplate.keys((key)).isEmpty());
    }

    public List<Long> getOnlineUser(List<Long> userIds) {
        return new LinkedList<>(getOnlineTerminal(userIds).keySet());
    }
}
