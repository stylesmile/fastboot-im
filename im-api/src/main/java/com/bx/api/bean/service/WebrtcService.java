package com.bx.api.bean.service;

import com.bx.imclient.IMClient;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.api.config.ICEServer;
import com.bx.api.config.ICEServerConfig;
import com.bx.api.contant.RedisKey;
import com.bx.api.enums.MessageType;
import com.bx.api.exception.GlobalException;
;
import com.bx.api.session.UserSession;
import com.bx.api.session.WebrtcSession;
import com.bx.api.vo.PrivateMessageVO;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.jedis.JedisTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class WebrtcService {

    @AutoWired
    private IMClient imClient;
    @AutoWired
    private JedisTemplate jedisTemplate;
    @AutoWired
    private ICEServerConfig iceServerConfig;
    @AutoWired
    private SessionService sessionService;
//    //@Override
    public void call(Long uid, String offer) {
        UserSession session = sessionService.getSession();;
        if (!imClient.isOnline(uid)) {
            throw new GlobalException("对方目前不在线");
        }
        // 创建webrtc会话
        WebrtcSession webrtcSession = new WebrtcSession();
        webrtcSession.setCallerId(session.getUserId());
        webrtcSession.setCallerTerminal(session.getTerminal());
        String key = getSessionKey(session.getUserId(), uid);
//        redisTemplate.opsForValue().set(key, webrtcSession, 12, TimeUnit.HOURS);
        // 12小时
        jedisTemplate.setSerializeDataEx(key, webrtcSession, 43200);
        // 向对方所有终端发起呼叫
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_CALL.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(offer);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);

    }

//    //@Override
    public void accept(Long uid, String answer) {
        UserSession session = sessionService.getSession();;
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 更新接受者信息
        webrtcSession.setAcceptorId(session.getUserId());
        webrtcSession.setAcceptorTerminal(session.getTerminal());
        String key = getSessionKey(session.getUserId(), uid);
//        redisTemplate.opsForValue().set(key, webrtcSession, 12, TimeUnit.HOURS);
        jedisTemplate.setSerializeDataEx(key, webrtcSession, 43200);
        // 向发起人推送接受通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_ACCEPT.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(answer);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        // 告知其他终端已经接受会话,中止呼叫
        sendMessage.setSendToSelf(true);
        sendMessage.setSendResult(false);
        sendMessage.setRecvTerminals((Collections.singletonList(webrtcSession.getCallerTerminal())));
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

//    //@Override
    public void reject(Long uid) {
        UserSession session = sessionService.getSession();;
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        removeWebrtcSession(uid, session.getUserId());
        // 向发起人推送拒绝通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_REJECT.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        // 告知其他终端已经拒绝会话,中止呼叫
        sendMessage.setSendToSelf(true);
        sendMessage.setSendResult(false);
        sendMessage.setRecvTerminals(Collections.singletonList(webrtcSession.getCallerTerminal()));
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

//    //@Override
    public void cancel(Long uid) {
        UserSession session = sessionService.getSession();;
        // 删除会话信息
        removeWebrtcSession(session.getUserId(), uid);
        // 向对方所有终端推送取消通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_ACCEPT.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        // 通知对方取消会话
        imClient.sendPrivateMessage(sendMessage);
    }

    //@Override
    public void failed(Long uid, String reason) {
        UserSession session = sessionService.getSession();;
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        removeWebrtcSession(uid, session.getUserId());
        // 向发起方推送通话失败信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_FAILED.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        // 告知其他终端已经会话失败,中止呼叫
        sendMessage.setSendToSelf(true);
        sendMessage.setSendResult(false);
        sendMessage.setRecvTerminals(Collections.singletonList(webrtcSession.getCallerTerminal()));
        sendMessage.setData(messageInfo);
        // 通知对方取消会话
        imClient.sendPrivateMessage(sendMessage);

    }

    //@Override
    public void leave(Long uid) {
        UserSession session = sessionService.getSession();;
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        removeWebrtcSession(uid, session.getUserId());
        // 向对方推送挂断通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_HANDUP.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        Integer terminal = getTerminalType(uid, webrtcSession);
        sendMessage.setRecvTerminals(Collections.singletonList(terminal));
        sendMessage.setData(messageInfo);
        // 通知对方取消会话
        imClient.sendPrivateMessage(sendMessage);
    }

    //@Override
    public void candidate(Long uid, String candidate) {
        UserSession session = sessionService.getSession();;
        // 查询webrtc会话
        WebrtcSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 向发起方推送同步candidate信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_CANDIDATE.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(candidate);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        Integer terminal = getTerminalType(uid, webrtcSession);
        sendMessage.setRecvTerminals(Collections.singletonList(terminal));
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

    //@Override
    public List<ICEServer> getIceServers() {
        return iceServerConfig.getIceServers();
    }

    private WebrtcSession getWebrtcSession(Long userId, Long uid) {
        String key = getSessionKey(userId, uid);
//        WebrtcSession webrtcSession = (WebrtcSession) redisTemplate.opsForValue().get(key);
        WebrtcSession webrtcSession = jedisTemplate.getSerializeData(key, WebrtcService.class);
        if (webrtcSession == null) {
            throw new GlobalException("视频通话已结束");
        }
        return webrtcSession;
    }

    private void removeWebrtcSession(Long userId, Long uid) {
        String key = getSessionKey(userId, uid);
//        redisTemplate.delete(key);
        jedisTemplate.delete(key);
    }

    private String getSessionKey(Long id1, Long id2) {
        Long minId = id1 > id2 ? id2 : id1;
        Long maxId = id1 > id2 ? id1 : id2;
        return String.join(":", RedisKey.IM_WEBRTC_SESSION, minId.toString(), maxId.toString());
    }

    private Integer getTerminalType(Long uid, WebrtcSession webrtcSession) {
        if (uid.equals(webrtcSession.getCallerId())) {
            return webrtcSession.getCallerTerminal();
        }
        return webrtcSession.getAcceptorTerminal();
    }

}
