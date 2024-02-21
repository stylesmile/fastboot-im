package com.bx.imserver;

import com.bx.imserver.task.PullMessageTask;
import com.bx.imserver.websocket.WebsocketStarter;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Controller;
import io.github.stylesmile.app.App;

@Controller
public class IMServerApp {
    @AutoWired
    static PullMessageTask pullMessageTask2;

    public static void main(String[] args) throws Exception {

        App.start(IMServerApp.class, args);
        //启动websocket服务
        WebsocketStarter.start();
        // 拉取消息
        pullMessageTask2.run();
    }

}
