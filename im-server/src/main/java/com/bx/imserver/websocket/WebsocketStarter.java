package com.bx.imserver.websocket;

import com.bx.imcommon.contant.IMRedisKey;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.tool.FastbootUtil;
import org.tio.server.ServerTioConfig;
import org.tio.utils.jfinal.P;
import org.tio.websocket.server.WsServerStarter;
import redis.clients.jedis.Jedis;

/**
 *
 */
public class WebsocketStarter {
    public static volatile long serverId = 0;

    private WsServerStarter wsServerStarter;
    private ServerTioConfig serverTioConfig;

//    @Value("ws.use.ssl")
//    String ssl;
//    @Value("ssl.keystore")
//    String keyStoreFile;
//    @Value("ssl.truststore")
//    String trustStoreFile;
//    @Value("ssl.pwd")
//    String keyStorePwd;

    public WebsocketStarter(int serverPort, WsMsgHandler wsMsgHandler) throws Exception {
        wsServerStarter = new WsServerStarter(serverPort, wsMsgHandler);

        serverTioConfig = wsServerStarter.getServerTioConfig();
        serverTioConfig.setName(WsConstant.PROTOCOL_NAME);
        serverTioConfig.setServerAioListener(ShowcaseServerAioListener.me);

        //设置ip监控
        serverTioConfig.setIpStatListener(ShowcaseIpStatListener.me);
        //设置ip统计时间段
        serverTioConfig.ipStats.addDurations(WsConstant.IpStatDuration.IPSTAT_DURATIONS);

        //设置心跳超时时间
        serverTioConfig.setHeartbeatTimeout(WsConstant.HEARTBEAT_TIMEOUT);
//        if (P.getInt("ws.use.ssl", 1) == 1) {
        //如果你希望通过wss来访问，就加上下面的代码吧，不过首先你得有SSL证书（证书必须和域名相匹配，否则可能访问不了ssl）
//			String keyStoreFile = "classpath:config/ssl/keystore.jks";
//			String trustStoreFile = "classpath:config/ssl/keystore.jks";
//			String keyStorePwd = "08gUMx4x";
//            serverTioConfig.useSsl(keyStoreFile, trustStoreFile, keyStorePwd);
//        }
    }


    /**
     * 启动
     *
     * @throws Exception e
     */
    public static void start() throws Exception {
        Jedis jedisTemplate = FastbootUtil.getBean(Jedis.class);
        String key = IMRedisKey.IM_MAX_SERVER_ID;
        serverId = jedisTemplate.incrBy(key, 1);
        WebsocketStarter appStarter = new WebsocketStarter(WsConstant.SERVER_PORT, WsMsgHandler.me);
        appStarter.wsServerStarter.start();
    }

    /**
     * @return the serverTioConfig
     */
    public ServerTioConfig getServerTioConfig() {
        return serverTioConfig;
    }

    public WsServerStarter getWsServerStarter() {
        return wsServerStarter;
    }

    public static void main(String[] args) throws Exception {
        //启动http server，这个步骤不是必须的，但是为了用页面演示websocket，所以先启动http
        P.use("app.properties");
        //启动websocket server
        start();
    }

}
