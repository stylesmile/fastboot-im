package com.bx.imclient.task;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.model.IMSendResult;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.ioc.Value;
import io.github.stylesmile.jedis.JedisTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageResultResultTask extends AbstractMessageResultTask {

    //    @Resource(name = "IMRedisTemplate")
//    private RedisTemplate<String, Object> redisTemplate;
    @AutoWired
    JedisTemplate jedisTemplate;

    @Value("fast.name")
    private String appName;

    @Value("im.result.batch:100")
    private int batchSize;

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
        List<IMSendResult> results;
        do {
            results = loadBatch();
            if (!results.isEmpty()) {
                listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, results);
            }
        } while (results.size() >= batchSize);
    }

    List<IMSendResult> loadBatch() {
        String key = StrUtil.join(":", IMRedisKey.IM_RESULT_PRIVATE_QUEUE, appName);
        //这个接口redis6.2以上才支持
        //List<Object> list = redisTemplate.opsForList().leftPop(key, batchSize);
        List<IMSendResult> results = new LinkedList<>();
//        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
        JSONObject jsonObject = jedisTemplate.lpopSerializeData(key, JSONObject.class);

        while (!Objects.isNull(jsonObject) && results.size() < batchSize) {
            results.add(jsonObject.toJavaObject(IMSendResult.class));
//            jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
            jsonObject = jedisTemplate.lpopSerializeData(key, JSONObject.class);

        }
        return results;
    }

}
