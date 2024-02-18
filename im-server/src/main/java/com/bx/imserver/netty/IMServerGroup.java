package com.bx.imserver.netty;

import com.bx.imcommon.contant.IMRedisKey;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.ioc.BeanContainer;
import io.github.stylesmile.jedis.JedisTemplate;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Service
public class IMServerGroup {
    @AutoWired
    JedisTemplate jedisTemplate;
    @AutoWired
    Jedis jedis;

    public static volatile long serverId = 0;
    public static List<IMServer> imServers = new ArrayList(){{
        add(new IMServer() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void start() {

            }

            @Override
            public void stop() {

            }
        });
    }};


    public IMServerGroup(List<IMServer> imServers) {
        this.imServers = imServers;
    }

    public IMServerGroup() {
    }

    /***
     * 判断服务器是否就绪
     *
     * @return
     **/
    public boolean isReady() {
        for (IMServer imServer : imServers) {
            if (!imServer.isReady()) {
                return false;
            }
        }
        return true;
    }

    @PostConstruct
    public void run(String... args) throws Exception {
        // 初始化SERVER_ID
        String key = IMRedisKey.IM_MAX_SERVER_ID;
//        serverId = redisTemplate.opsForValue().increment(key, 1);
        // 启动服务
        for (IMServer imServer : imServers) {
            imServer.start();
        }
//        serverId = jedis.incrBy(key,1);
//        serverId = jedisTemplate.incrLongData(key, 1);
        System.out.println("serverId:" + serverId);
    }

    @PreDestroy
    public void destroy() {
        // 停止服务
        for (IMServer imServer : imServers) {
            imServer.stop();
        }
    }
}
