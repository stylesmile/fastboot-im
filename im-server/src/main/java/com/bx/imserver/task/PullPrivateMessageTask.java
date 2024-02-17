package com.bx.imserver.task;

import com.alibaba.fastjson.JSONObject;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.processor.AbstractMessageProcessor;
import com.bx.imserver.netty.processor.ProcessorFactory;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullPrivateMessageTask extends AbstractPullMessageTask {

    private final JedisTemplate redisTemplate;

    @Override
    public void pullMessage() {
        // 从redis拉取未读消息
        String key = String.join(":", IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, IMServerGroup.serverId + "");
//        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
        JSONObject jsonObject = redisTemplate.rpop(key, JSONObject.class);
        while (!Objects.isNull(jsonObject)) {
            IMRecvInfo recvInfo = jsonObject.toJavaObject(IMRecvInfo.class);
            AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.PRIVATE_MESSAGE);
            processor.process(recvInfo);
            // 下一条消息
//            jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
            jsonObject = redisTemplate.rpop(key, JSONObject.class);
        }
    }
}
