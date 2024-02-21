package com.bx.imserver.service;

import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imserver.netty.UserChannelCtxMap;
import com.google.gson.JsonObject;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.tool.FastbootUtil;
import io.github.stylesmile.tool.JsonGsonUtil;
import io.github.stylesmile.websocket.WebsocketUtil;
import org.tio.core.ChannelContext;

@Service
public class DealWebsocketMessageService {
    @AutoWired
    private JedisTemplate redisTemplate;

    public void deal(String text, ChannelContext channelContext) {
        JsonObject jsonObject = JsonGsonUtil.GsonToBean(text, JsonObject.class);
        Integer cmd = jsonObject.get("cmd").getAsInt();
        if (null == cmd) {
            return;
        }
        String token = jsonObject.get("data").getAsJsonObject().get("accessToken").getAsString();
        JsonObject userSession = redisTemplate.getSerializeData(
                String.format(IMRedisKey.TOKEN_USER_SESSION, token),
                JsonObject.class);
        switch (cmd) {
            case 0:
                // 登录
                LoginService loginService = FastbootUtil.getBean(LoginService.class);
                loginService.process(userSession, channelContext);
                break;
            case 1:
                // 心跳
                HeartbeatService heartbeatService = FastbootUtil.getBean(HeartbeatService.class);
                heartbeatService.process(userSession, channelContext);
            case 2:
                // 下线
                WebsocketUtil.remove(channelContext);
                Long userId = userSession.get("userId").getAsLong();
                Integer terminal = userSession.get("terminal").getAsInt();
                UserChannelCtxMap.removeChannelCtx(userId, terminal);
            default:
                break;
        }
    }
}
