package com.bx.api.bean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.api.common.util.BeanUtils;
import com.bx.api.common.util.SensitiveFilterUtil;
import com.bx.api.domain.dto.PrivateMessageDTO;
import com.bx.api.domain.entity.Friend;
import com.bx.api.domain.entity.PrivateMessage;
import com.bx.api.common.enums.MessageStatus;
import com.bx.api.common.enums.MessageType;
import com.bx.api.common.enums.ResultCode;
import com.bx.api.common.exception.GlobalException;
import com.bx.api.mapper.PrivateMessageMapper;
import com.bx.api.domain.dto.session.UserSession;
import com.bx.api.domain.vo.PrivateMessageVO;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;
import java.util.stream.Collectors;

;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageService {
    //public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements IPrivateMessageService {
    @AutoWired
    private FriendService friendService;
    @AutoWired
    private IMClient imClient;
    @AutoWired
    private SensitiveFilterUtil sensitiveFilterUtil;
    @AutoWired
    private PrivateMessageMapper privateMessageMapper;
    @AutoWired
    private SessionService sessionService;

    //@Override
    public Long sendMessage(PrivateMessageDTO dto) {
        UserSession session = sessionService.getSession();
        ;
        Boolean isFriends = friendService.isFriend(session.getUserId(), dto.getRecvId());
        if (Boolean.FALSE.equals(isFriends)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不是对方好友，无法发送消息");
        }
        // 保存消息
        PrivateMessage msg = BeanUtils.copyProperties(dto, PrivateMessage.class);
        msg.setSendId(session.getUserId());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setSendTime(new Date());
//        this.save(msg);
        privateMessageMapper.insert(msg);
        // 过滤消息内容
        String content = sensitiveFilterUtil.filter(dto.getContent());
        msg.setContent(content);
        // 推送消息
        PrivateMessageVO msgInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(msgInfo.getRecvId());
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(true);
        imClient.sendPrivateMessage(sendMessage);
        log.info("发送私聊消息，发送id:{},接收id:{}，内容:{}", session.getUserId(), dto.getRecvId(), dto.getContent());
        return msg.getId();
    }

    //@Override
    public void recallMessage(Long id) {
        UserSession session = sessionService.getSession();
        ;
//        PrivateMessage msg = this.getById(id);
        PrivateMessage msg = privateMessageMapper.selectById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息已发送超过5分钟，无法撤回");
        }
        // 修改消息状态
        msg.setStatus(MessageStatus.RECALL.code());
//        this.updateById(msg);
        privateMessageMapper.updateById(msg);
        // 推送消息
        PrivateMessageVO msgInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        msgInfo.setType(MessageType.RECALL.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setContent("对方撤回了一条消息");

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(msgInfo.getRecvId());
        sendMessage.setSendToSelf(false);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);

        // 推给自己其他终端
        msgInfo.setContent("你撤回了一条消息");
        sendMessage.setSendToSelf(true);
        sendMessage.setRecvTerminals(Collections.emptyList());
        imClient.sendPrivateMessage(sendMessage);
        log.info("撤回私聊消息，发送id:{},接收id:{}，内容:{}", msg.getSendId(), msg.getRecvId(), msg.getContent());
    }


    //@Override
    public List<PrivateMessageVO> findHistoryMessage(Long friendId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = sessionService.getSession().getUserId();
        long stIdx = (page - 1) * size;
        QueryWrapper<PrivateMessage> wrapper = new QueryWrapper<>();
        wrapper.lambda().and(wrap -> wrap.and(
                                wp -> wp.eq(PrivateMessage::getSendId, userId)
                                        .eq(PrivateMessage::getRecvId, friendId))
                        .or(wp -> wp.eq(PrivateMessage::getRecvId, userId)
                                .eq(PrivateMessage::getSendId, friendId)))
                .ne(PrivateMessage::getStatus, MessageStatus.RECALL.code())
                .orderByDesc(PrivateMessage::getId)
                .last("limit " + stIdx + "," + size);
//        List<PrivateMessage> messages = this.list(wrapper);
        List<PrivateMessage> messages = privateMessageMapper.selectList(wrapper);
        List<PrivateMessageVO> messageInfos = messages.stream().map(m -> BeanUtils.copyProperties(m, PrivateMessageVO.class)).collect(Collectors.toList());
        log.info("拉取聊天记录，用户id:{},好友id:{}，数量:{}", userId, friendId, messageInfos.size());
        return messageInfos;
    }


    //@Override
    public List<PrivateMessageVO> loadMessage(Long minId) {
        UserSession session = sessionService.getSession();
        ;
        List<Friend> friends = friendService.findFriendByUserId(session.getUserId());
        if (friends.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> friendIds = friends.stream().map(Friend::getFriendId).collect(Collectors.toList());
        // 获取当前用户的消息
        LambdaQueryWrapper<PrivateMessage> queryWrapper = Wrappers.lambdaQuery();
        // 只能拉取最近1个月的
        Date minDate = DateUtils.addMonths(new Date(), -1);
        queryWrapper.gt(PrivateMessage::getId, minId)
                .ge(PrivateMessage::getSendTime, minDate)
                .ne(PrivateMessage::getStatus, MessageStatus.RECALL.code())
                .and(wrap -> wrap.and(
                                wp -> wp.eq(PrivateMessage::getSendId, session.getUserId())
                                        .in(PrivateMessage::getRecvId, friendIds))
                        .or(wp -> wp.eq(PrivateMessage::getRecvId, session.getUserId())
                                .in(PrivateMessage::getSendId, friendIds)))
                .orderByAsc(PrivateMessage::getId)
                .last("limit 100");

//        List<PrivateMessage> messages = this.list(queryWrapper);
        List<PrivateMessage> messages = privateMessageMapper.selectList(queryWrapper);
        // 更新发送状态
        List<Long> ids = messages.stream()
                .filter(m -> !m.getSendId().equals(session.getUserId()) && m.getStatus().equals(MessageStatus.UNSEND.code()))
                .map(PrivateMessage::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            LambdaUpdateWrapper<PrivateMessage> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.in(PrivateMessage::getId, ids)
                    .set(PrivateMessage::getStatus, MessageStatus.SENDED.code());
//            this.update(updateWrapper);

            privateMessageMapper.update(updateWrapper);
        }
        log.info("拉取消息，用户id:{},数量:{}", session.getUserId(), messages.size());
        return messages.stream().map(m -> BeanUtils.copyProperties(m, PrivateMessageVO.class)).collect(Collectors.toList());
    }


    //    @Transactional(rollbackFor = Exception.class)
    //@Override
    public void readedMessage(Long friendId) {
        UserSession session = sessionService.getSession();
        ;
        // 推送消息
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setRecvId(friendId);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(friendId);
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);
        // 修改消息状态为已读
        LambdaUpdateWrapper<PrivateMessage> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(PrivateMessage::getSendId, friendId)
                .eq(PrivateMessage::getRecvId, session.getUserId())
                .eq(PrivateMessage::getStatus, MessageStatus.SENDED.code())
                .set(PrivateMessage::getStatus, MessageStatus.READED.code());
//        this.update(updateWrapper);
        privateMessageMapper.update(updateWrapper);
        log.info("消息已读，接收方id:{},发送方id:{}", session.getUserId(), friendId);
    }


    //@Override
    public Long getMaxReadedId(Long friendId) {
        UserSession session = sessionService.getSession();
        ;
        LambdaQueryWrapper<PrivateMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PrivateMessage::getSendId, session.getUserId())
                .eq(PrivateMessage::getRecvId, friendId)
                .eq(PrivateMessage::getStatus, MessageStatus.READED.code())
                .orderByDesc(PrivateMessage::getId)
                .select(PrivateMessage::getId)
                .last("limit 1");
//        PrivateMessage message = this.getOne(wrapper);
        PrivateMessage message = privateMessageMapper.selectOne(wrapper);
        if (Objects.isNull(message)) {
            return -1L;
        }
        return message.getId();
    }
}
