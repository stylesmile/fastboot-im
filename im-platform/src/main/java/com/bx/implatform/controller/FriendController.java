package com.bx.implatform.controller;

import com.bx.implatform.entity.Friend;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IFriendService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.vo.FriendVO;
import io.github.stylesmile.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "好友")
@Controller
public class FriendController {
    @AutoWired
    private IFriendService friendService;

    //    @GetMapping("/friend/list")
    @RequestMapping("/friend/list")
    @ApiOperation(value = "好友列表", notes = "获取好友列表")
    public Result<List<FriendVO>> findFriends() {
        List<Friend> friends = friendService.findFriendByUserId(SessionContext.getSession().getUserId());
        List<FriendVO> vos = friends.stream().map(f -> {
            FriendVO vo = new FriendVO();
            vo.setId(f.getFriendId());
            vo.setHeadImage(f.getFriendHeadImage());
            vo.setNickName(f.getFriendNickName());
            return vo;
        }).collect(Collectors.toList());
        return ResultUtils.success(vos);
    }


    //    @PostMapping("/friend/add")
    @RequestMapping("/friend/add")
    @ApiOperation(value = "添加好友", notes = "双方建立好友关系")
    public Result addFriend(@NotEmpty(message = "好友id不可为空") @RequestParam("friendId") Long friendId) {
        friendService.addFriend(friendId);
        return ResultUtils.success();
    }

    //    @GetMapping("/friend/find/{friendId}")
//    @RequestMapping("/friend/find/{friendId}")
    @RequestMapping("/friend/find/{friendId}")
    @ApiOperation(value = "查找好友信息", notes = "查找好友信息")
//    public Result<FriendVO> findFriend(@NotEmpty(message = "好友id不可为空") @PathVariable("friendId") Long friendId) {
    public Result<FriendVO> findFriend(@NotEmpty(message = "好友id不可为空") Long friendId) {
        return ResultUtils.success(friendService.findFriend(friendId));
    }


    //    @DeleteMapping("/friend/delete/{friendId}")
    @RequestMapping("/friend/delete/{friendId}")
    @ApiOperation(value = "删除好友", notes = "解除好友关系")
//    public Result delFriend(@NotEmpty(message = "好友id不可为空") @PathVariable("friendId") Long friendId) {
    public Result delFriend(@NotEmpty(message = "好友id不可为空") Long friendId) {
        friendService.delFriend(friendId);
        return ResultUtils.success();
    }

    //    @PutMapping("/friend/update")
    @RequestMapping("/friend/update")
    @ApiOperation(value = "更新好友信息", notes = "更新好友头像或昵称")
    public Result modifyFriend(@Valid @RequestBody FriendVO vo) {
        friendService.update(vo);
        return ResultUtils.success();
    }


}

