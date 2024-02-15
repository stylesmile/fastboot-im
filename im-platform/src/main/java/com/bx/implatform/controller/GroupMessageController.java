package com.bx.implatform.controller;

import com.bx.implatform.dto.GroupMessageDTO;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IGroupMessageService;
import com.bx.implatform.vo.GroupMessageVO;
import io.github.stylesmile.annotation.Controller;
import io.github.stylesmile.annotation.RequestMapping;
import io.github.stylesmile.annotation.RequestParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "群聊消息")
@Controller
@RequiredArgsConstructor
public class GroupMessageController {

    private final IGroupMessageService groupMessageService;

    //    @PostMapping("/message/group/send")
    @RequestMapping("/message/group/send")
    @ApiOperation(value = "发送群聊消息", notes = "发送群聊消息")
    public Result<Long> sendMessage(@Valid  GroupMessageDTO vo) {
        return ResultUtils.success(groupMessageService.sendMessage(vo));
    }

    //    @DeleteMapping("/message/group/recall/{id}")
    @RequestMapping("/message/group/recall/{id}")
    @ApiOperation(value = "撤回消息", notes = "撤回群聊消息")
    public Result<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id) {
        groupMessageService.recallMessage(id);
        return ResultUtils.success();
    }


    //    @GetMapping("/message/group/loadMessage")
    @RequestMapping("/message/group/loadMessage")
    @ApiOperation(value = "拉取消息", notes = "拉取消息,一次最多拉取100条")
    public Result<List<GroupMessageVO>> loadMessage(@RequestParam Long minId) {
        return ResultUtils.success(groupMessageService.loadMessage(minId));
    }


    //    @PutMapping("/message/group/readed")
    @RequestMapping("/message/group/readed")
    @ApiOperation(value = "消息已读", notes = "将群聊中的消息状态置为已读")
    public Result readedMessage(@RequestParam("groupId") Long groupId) {
        groupMessageService.readedMessage(groupId);
        return ResultUtils.success();
    }


    //    @GetMapping("/message/group/history")
    @RequestMapping("/message/group/history")
    @ApiOperation(value = "查询聊天记录", notes = "查询聊天记录")
    public Result<List<GroupMessageVO>> recallMessage(
            @NotNull(message = "群聊id不能为空") @RequestParam("groupId") Long groupId,
            @NotNull(message = "页码不能为空") @RequestParam("page") Long page,
            @NotNull(message = "size不能为空") @RequestParam("size") Long size) {
        return ResultUtils.success(groupMessageService.findHistoryMessage(groupId, page, size));
    }
}

