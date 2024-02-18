package com.bx.imserver;

import com.bx.imserver.netty.IMServer;
import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.ws.WebSocketServer;
import com.bx.imserver.task.AbstractPullMessageTask;
import com.bx.imserver.task.PullGroupMessageTask;
import com.bx.imserver.task.PullPrivateMessageTask;
import com.bx.imserver.task.PullPrivateMessageTask2;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Controller;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.app.App;

import java.util.ArrayList;
import java.util.List;

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
    static PullPrivateMessageTask2 pullPrivateMessageTask2;

    //    public static void main(String[] args) {
//        SpringApplication.run(IMServerApp.class, args);
//    }
    public static void main(String[] args) throws Exception {

        App.start(IMServerApp.class, args);

        webSocketServer.start();
//        PullGroupMessageTask pullGroupMessageTask = new PullGroupMessageTask();
//        PullPrivateMessageTask2 pullPrivateMessageTask2 = new PullPrivateMessageTask2();
//        pullPrivateMessageTask2.run();
//        PullPrivateMessageTask pullPrivateMessageTask = new PullPrivateMessageTask();
//        pullPrivateMessageTask.run();
        pullPrivateMessageTask2.run();
    }

}
