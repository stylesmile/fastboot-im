package com.bx.imserver.task;

import com.alibaba.fastjson.JSONObject;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imcommon.util.ThreadPoolExecutorFactory;
import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.processor.PrivateMessageService;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.tool.FastbootUtil;
import io.github.stylesmile.tool.GsonByteUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PullMessageTask2 {
    private static int corePoolSize = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, corePoolSize * 4, 50L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000));
    private static ExecutorService EXECUTOR_SERVICE = ThreadPoolExecutorFactory.getThreadPoolExecutor();
    @AutoWired
    IMServerGroup imServerGroup;
    @AutoWired
    private JedisTemplate redisTemplate;
    @AutoWired
    private Jedis jedis;

    public void run(String... args) {
        executor.execute(new Runnable() {
            //        EXECUTOR_SERVICE.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                try {
//                    if (imServerGroup.isReady()) {
                    pullMessagePrivate();
//                    }
                } catch (Exception e) {
                    log.error("任务调度异常", e);
                    //e.printStackTrace();
                }
                if (!EXECUTOR_SERVICE.isShutdown()) {
                    Thread.sleep(100);
                    EXECUTOR_SERVICE.execute(this);
                }
            }
        });
        executor.execute(new Runnable() {
            //        EXECUTOR_SERVICE.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                try {
//                    if (imServerGroup.isReady()) {
                    pullMessageGroup();
//                    }
                } catch (Exception e) {
                    log.error("任务调度异常", e);
                    //e.printStackTrace();
                }
                if (!EXECUTOR_SERVICE.isShutdown()) {
                    Thread.sleep(100);
                    EXECUTOR_SERVICE.execute(this);
                }
            }
        });
    }


    public void pullMessagePrivate() {
        // 从redis拉取未读消息
        String key = String.join(":", IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, IMServerGroup.serverId + "");
//        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
//        JSONObject jsonObject = redisTemplate.rpop(key, JSONObject.class);
        JSONObject jsonObject = redisTemplate.rpopSerializeData(key, JSONObject.class);
//        JSONObject jsonObject = redisTemplate.rpop(key, JSONObject.class);
        while (!Objects.isNull(jsonObject)) {
            IMRecvInfo recvInfo = jsonObject.toJavaObject(IMRecvInfo.class);
//            AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.PRIVATE_MESSAGE);
            PrivateMessageService processor = FastbootUtil.getBean(PrivateMessageService.class);
            processor.process(recvInfo);
            // 下一条消息
//            jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
            byte[] bytes = jedis.lpop(GsonByteUtils.toByteArray(key));
            if (bytes != null && bytes.length > 0) {
                jsonObject = GsonByteUtils.fromByteArray(bytes, JSONObject.class);
            }
        }
    }

    public void pullMessageGroup() {
        // 从redis拉取未读消息
        String key = String.join(":", IMRedisKey.IM_MESSAGE_GROUP_QUEUE, IMServerGroup.serverId + "");
//        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
        JSONObject jsonObject = redisTemplate.lpopSerializeData(key, JSONObject.class);
        while (!Objects.isNull(jsonObject)) {
            IMRecvInfo recvInfo = jsonObject.toJavaObject(IMRecvInfo.class);
//            AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.GROUP_MESSAGE);
//            processor.process(recvInfo);
//            GroupMessageProcessor processor = FastbootUtil.getBean(GroupMessageProcessor.class);
//            processor.process(recvInfo);
            // 下一条消息
//            jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
            jsonObject = redisTemplate.lpopSerializeData(key, JSONObject.class);
        }
    }
}
