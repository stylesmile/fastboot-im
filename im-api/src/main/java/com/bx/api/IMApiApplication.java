package com.bx.api;

import com.bx.imclient.IMClient;
import com.bx.imclient.sender.IMSender;
import io.github.stylesmile.annotation.Fastboot;
import io.github.stylesmile.app.App;
// IMClient在子包中，需要添加

@Fastboot(include = {IMClient.class, IMSender.class})
public class IMApiApplication {

    public static void main(String[] args) {
        App.start(IMApiApplication.class, args);
    }
}
