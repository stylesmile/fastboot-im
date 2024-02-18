package com.bx.api.bean.controller;

import com.bx.api.domain.dto.PrivateMessageDTO;
import com.bx.api.common.result.Result;
import com.bx.api.common.result.ResultUtils;
import com.bx.api.bean.service.PrivateMessageService;
import com.bx.api.domain.vo.PrivateMessageVO;
import io.github.stylesmile.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "私聊消息")
@Controller
public class PrivateMessageController {
    @AutoWired

    private PrivateMessageService privateMessageService;

    //    @PostMapping("/message/private/send")
    @RequestMapping("/message/private/send")
    @ApiOperation(value = "发送消息", notes = "发送私聊消息")
    public Result<Long> sendMessage(@Valid @RequestBody PrivateMessageDTO vo) {
        return ResultUtils.success(privateMessageService.sendMessage(vo));
    }


    //    @DeleteMapping("/message/private/recall/{id}")
    @RequestMapping("/message/private/recall")
    @ApiOperation(value = "撤回消息", notes = "撤回私聊消息")
//    public Result<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id) {
    public Result<Long> recallMessage(@NotNull(message = "消息id不能为空") Long id) {
        privateMessageService.recallMessage(id);
        return ResultUtils.success();
    }


    //    @GetMapping("/message/private/loadMessage")
    @RequestMapping("/message/private/loadMessage")
    @ApiOperation(value = "拉取消息", notes = "拉取消息,一次最多拉取100条")
    public Result<List<PrivateMessageVO>> loadMessage(@RequestParam("minId") Long minId) {
        return ResultUtils.success(privateMessageService.loadMessage(minId));
    }

    //    @PutMapping("/message/private/readed")
    @RequestMapping("/message/private/readed")
    @ApiOperation(value = "消息已读", notes = "将会话中接收的消息状态置为已读")
    public Result readedMessage(@RequestParam("friendId") Long friendId) {
        privateMessageService.readedMessage(friendId);
        return ResultUtils.success();
    }

    //    @GetMapping("/message/private/maxReadedId")
    @RequestMapping("/message/private/maxReadedId")
    @ApiOperation(value = "获取最大已读消息的id", notes = "获取某个会话中已读消息的最大id")
    public Result<Long> getMaxReadedId(@RequestParam("friendId") Long friendId) {
        return ResultUtils.success(privateMessageService.getMaxReadedId(friendId));
    }

    //    @GetMapping("/message/private/history")
    @RequestMapping("/message/private/history")
    @ApiOperation(value = "查询聊天记录", notes = "查询聊天记录")
    public Result<List<PrivateMessageVO>> recallMessage(
            @NotNull(message = "好友id不能为空") @RequestParam("friendId") Long friendId,
            @NotNull(message = "页码不能为空") @RequestParam("page") Long page,
            @NotNull(message = "size不能为空") @RequestParam("size") Long size) {
        return ResultUtils.success(privateMessageService.findHistoryMessage(friendId, page, size));
    }

}

