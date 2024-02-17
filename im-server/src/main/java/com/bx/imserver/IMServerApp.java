package com.bx.imserver;

import io.github.stylesmile.app.App;

//@EnableAsync
//@EnableScheduling
//@ComponentScan(basePackages = {"com.bx"})
//@SpringBootApplication
public class IMServerApp {

    //    public static void main(String[] args) {
//        SpringApplication.run(IMServerApp.class, args);
//    }
    public static void main(String[] args) {
        App.start(IMServerApp.class, args);
    }

}
