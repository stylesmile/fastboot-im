package com.bx.imserver;

import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.ws.WebSocketServer;
import com.bx.imserver.task.PullMessageTask;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Controller;
import io.github.stylesmile.app.App;

//@EnableAsync
//@EnableScheduling
//@ComponentScan(basePackages = {"com.bx"})
//@SpringBootApplication
@Controller
public class IMServerApp {
    @AutoWired
    static WebSocketServer webSocketServer;
    @AutoWired
    static IMServerGroup imServerGroup;
    @AutoWired
    static PullMessageTask pullMessageTask;

    //    public static void main(String[] args) {
//        SpringApplication.run(IMServerApp.class, args);
//    }
    public static void main(String[] args) throws Exception {

        App.start(IMServerApp.class, args);
        //启动websocket服务
        webSocketServer.start();
        // 拉取消息
        pullMessageTask.run();
    }

}
