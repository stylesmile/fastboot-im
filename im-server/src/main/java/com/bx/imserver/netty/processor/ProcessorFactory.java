//package com.bx.imserver.netty.processor;
//
//import com.bx.imcommon.enums.IMCmdType;
//import io.github.stylesmile.ioc.BeanContainer;
//
//public class ProcessorFactory {
//
//    public static AbstractMessageProcessor createProcessor(IMCmdType cmd) {
//        AbstractMessageProcessor processor = null;
//        switch (cmd) {
//            case LOGIN:
//                processor = (LoginProcessor) getBean(LoginProcessor.class);
//                break;
//            case HEART_BEAT:
////                processor = SpringContextHolder.getApplicationContext().getBean(HeartbeatProcessor.class);
//                processor = (HeartbeatProcessor) getBean(HeartbeatProcessor.class);
//                break;
//            case PRIVATE_MESSAGE:
////                processor = SpringContextHolder.getApplicationContext().getBean(PrivateMessageProcessor.class);
//                processor = (PrivateMessageProcessor) getBean(PrivateMessageProcessor.class);
//
//                break;
//            case GROUP_MESSAGE:
////                processor = SpringContextHolder.getApplicationContext().getBean(GroupMessageProcessor.class);
//                processor = (GroupMessageProcessor) getBean(GroupMessageProcessor.class);
//
//                break;
//            default:
//                break;
//        }
//        return processor;
//
//    }
//
//    public static Object getBean(Class clazz) {
//        try {
//            Object o = BeanContainer.getInstance(clazz);
//            if (o != null) {
//                return o;
//            }
//            return clazz.newInstance();
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (InstantiationException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}
