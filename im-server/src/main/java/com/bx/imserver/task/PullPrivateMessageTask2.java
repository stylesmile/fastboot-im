package com.bx.imserver.task;

import com.alibaba.fastjson.JSONObject;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imcommon.util.ThreadPoolExecutorFactory;
import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.processor.AbstractMessageProcessor;
import com.bx.imserver.netty.processor.ProcessorFactory;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.tool.GsonByteUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class PullPrivateMessageTask2 {
    private static ExecutorService EXECUTOR_SERVICE = ThreadPoolExecutorFactory.getThreadPoolExecutor();
    @AutoWired
    IMServerGroup imServerGroup;
    @AutoWired
    private JedisTemplate redisTemplate;
    @AutoWired
    private Jedis jedis;
    public void run(String... args) {
        EXECUTOR_SERVICE.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                try {
//                    if (imServerGroup.isReady()) {
                        pullMessage();
//                    }
                } catch (Exception e) {
                    log.error("任务调度异常", e);
                    e.printStackTrace();
                }
                if (!EXECUTOR_SERVICE.isShutdown()) {
                    Thread.sleep(100);
                    EXECUTOR_SERVICE.execute(this);
                }
            }
        });
    }


    public void pullMessage() {
        // 从redis拉取未读消息
        String key = String.join(":", IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, IMServerGroup.serverId + "");
//        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
//        JSONObject jsonObject = redisTemplate.rpop(key, JSONObject.class);
        byte[] value = jedis.rpop(GsonByteUtils.toByteArray(key));

        JSONObject jsonObject = GsonByteUtils.fromByteArray(value,JSONObject.class);
//        JSONObject jsonObject = redisTemplate.rpop(key, JSONObject.class);
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
