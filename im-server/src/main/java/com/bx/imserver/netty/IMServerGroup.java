package com.bx.imserver.netty;

import com.bx.imcommon.contant.IMRedisKey;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.util.List;

@Slf4j
@Service
public class IMServerGroup {

    public static volatile long serverId = 0;

    //    RedisTemplate<String, Object> redisTemplate;
    @AutoWired
    JedisTemplate jedisTemplate;

    private final List<IMServer> imServers ;

    public IMServerGroup(List<IMServer> imServers) {
        this.imServers = imServers;
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

    public void run(String... args) throws Exception {
        // 初始化SERVER_ID
        String key = IMRedisKey.IM_MAX_SERVER_ID;
//        serverId = redisTemplate.opsForValue().increment(key, 1);
        serverId = jedisTemplate.incrLongData(key, 1);
        // 启动服务
        for (IMServer imServer : imServers) {
            imServer.start();
        }
    }

    @PreDestroy
    public void destroy() {
        // 停止服务
        for (IMServer imServer : imServers) {
            imServer.stop();
        }
    }
}
