package com.bx.implatform;

import com.bx.imclient.IMClient;
import io.github.stylesmile.annotation.Fastboot;
import io.github.stylesmile.app.App;
// IMClient在子包中，需要添加

@Fastboot(include = {IMClient.class})
public class IMPlatformApplication {

    public static void main(String[] args) {
        App.start(IMPlatformApplication.class, args);
    }
}
