package com.bx.implatform.controller;

import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IGroupService;
import com.bx.implatform.vo.GroupInviteVO;
import com.bx.implatform.vo.GroupMemberVO;
import com.bx.implatform.vo.GroupVO;
import io.github.stylesmile.annotation.Controller;
import io.github.stylesmile.annotation.RequestMapping;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "群聊")
@Controller
@RequiredArgsConstructor
public class GroupController {

    private final IGroupService groupService;

    @ApiOperation(value = "创建群聊", notes = "创建群聊")
//    @PostMapping("/group/create")
    @RequestMapping("/group/create")
    public Result<GroupVO> createGroup(@Valid @RequestBody GroupVO vo) {
        return ResultUtils.success(groupService.createGroup(vo));
    }

    @ApiOperation(value = "修改群聊信息", notes = "修改群聊信息")
//    @PutMapping("/group/modify")
    @RequestMapping("/group/modify")
    public Result<GroupVO> modifyGroup(@Valid @RequestBody GroupVO vo) {
        return ResultUtils.success(groupService.modifyGroup(vo));
    }

    @ApiOperation(value = "解散群聊", notes = "解散群聊")
//    @DeleteMapping("/group/delete/{groupId}")
    @RequestMapping("/group/delete/{groupId}")
    public Result deleteGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询群聊", notes = "查询单个群聊信息")
//    @GetMapping("/group/find/{groupId}")
    @RequestMapping("/group/find/{groupId}")
    public Result<GroupVO> findGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResultUtils.success(groupService.findById(groupId));
    }

    @ApiOperation(value = "查询群聊列表", notes = "查询群聊列表")
//    @GetMapping("/group/list")
    @RequestMapping("/group/list")
    public Result<List<GroupVO>> findGroups() {
        return ResultUtils.success(groupService.findGroups());
    }

    @ApiOperation(value = "邀请进群", notes = "邀请好友进群")
//    @PostMapping("/group/invite")
    @RequestMapping("/group/invite")
    public Result invite(@Valid @RequestBody GroupInviteVO vo) {
        groupService.invite(vo);
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询群聊成员", notes = "查询群聊成员")
//    @GetMapping("/group/members/{groupId}")
    @RequestMapping("/group/members/{groupId}")
    public Result<List<GroupMemberVO>> findGroupMembers(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResultUtils.success(groupService.findGroupMembers(groupId));
    }

    @ApiOperation(value = "退出群聊", notes = "退出群聊")
//    @DeleteMapping("/group/quit/{groupId}")
    @RequestMapping("/group/quit/{groupId}")
    public Result quitGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.quitGroup(groupId);
        return ResultUtils.success();
    }

    @ApiOperation(value = "踢出群聊", notes = "将用户踢出群聊")
//    @DeleteMapping("/group/kick/{groupId}")
    @RequestMapping("/group/kick/{groupId}")
    public Result kickGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
                            @NotNull(message = "用户id不能为空") @RequestParam Long userId) {
        groupService.kickGroup(groupId, userId);
        return ResultUtils.success();
    }

}

