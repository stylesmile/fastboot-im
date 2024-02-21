package com.bx.imserver.websocket.service;

import com.bx.imcommon.contant.IMRedisKey;
import com.google.gson.JsonObject;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import io.github.stylesmile.tool.FastbootUtil;
import io.github.stylesmile.tool.JsonGsonUtil;
import io.github.stylesmile.websocket.WebsocketUtil;
import org.tio.core.ChannelContext;
import org.tio.websocket.common.WsRequest;

@Service
public class DealWebsocketMessageService {
    @AutoWired
    private JedisTemplate redisTemplate;

    public void deal(WsRequest wsRequest, String text, ChannelContext channelContext) {
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
                LoginService loginService = FastbootUtil.getBean(LoginService.class);
                loginService.process(userSession, channelContext);
                break;
            case 1:
                HeartbeatService heartbeatService = FastbootUtil.getBean(HeartbeatService.class);
                heartbeatService.process(userSession, channelContext);
            case 2:
                // 下线
                WebsocketUtil.remove(channelContext);
//            case 3:
//                PrivateMessageService privateMessageService = FastbootUtil.getBean(PrivateMessageService.class);
//                privateMessageService.process(userSession, channelContext);
//            case 4:
//                GroupMessageProcessor groupMessageProcessor = FastbootUtil.getBean(GroupMessageProcessor.class);
//                groupMessageProcessor.process(userSession, channelContext);
            default:
                break;
        }
    }
}
