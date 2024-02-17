package com.bx.imserver;

import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.ws.WebSocketServer;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Controller;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.app.App;

//@EnableAsync
//@EnableScheduling
//@ComponentScan(basePackages = {"com.bx"})
//@SpringBootApplication
@Controller
public class IMServerApp {
    @AutoWired
    static WebSocketServer webSocketServer;

    //    public static void main(String[] args) {
//        SpringApplication.run(IMServerApp.class, args);
//    }
    public static void main(String[] args) {
        App.start(IMServerApp.class, args);
        webSocketServer.start();
    }

}
