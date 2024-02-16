package com.bx.implatform.controller;

import com.bx.implatform.entity.User;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.impl.UserServiceImpl;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.OnlineTerminalVO;
import com.bx.implatform.vo.UserVO;
import io.github.stylesmile.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Api(tags = "用户")
@Controller
public class UserController {
    @AutoWired
    private UserServiceImpl userService;

    //    @GetMapping("/user/terminal/online")
    @RequestMapping("/user/terminal/online")
    @ApiOperation(value = "判断用户哪个终端在线", notes = "返回在线的用户id的终端集合")
    public Result<List<OnlineTerminalVO>> getOnlineTerminal(@NotEmpty @RequestParam("userIds") String userIds) {
        return ResultUtils.success(userService.getOnlineTerminals(userIds));
    }


    //    @GetMapping("/user/self")
    @RequestMapping("/user/self")
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前用户信息")
    public Result<UserVO> findSelfInfo() {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        UserVO userVO = BeanUtils.copyProperties(user, UserVO.class);
        return ResultUtils.success(userVO);
    }


    //    @GetMapping("/user/find/{id}")
    @RequestMapping("/user/find/{id}")
    @ApiOperation(value = "查找用户", notes = "根据id查找用户")
//    public Result<UserVO> findById(@NotEmpty @PathVariable("id") Long id) {
    public Result<UserVO> findById(@NotEmpty Long id) {
        return ResultUtils.success(userService.findUserById(id));
    }

    //    @PutMapping("/user/update")
    @RequestMapping("/user/update")
    @ApiOperation(value = "修改用户信息", notes = "修改用户信息，仅允许修改登录用户信息")
    public Result update(@Valid @RequestBody UserVO vo) {
        userService.update(vo);
        return ResultUtils.success();
    }

    //    @GetMapping("/user/findByName")
    @RequestMapping("/user/findByName")
    @ApiOperation(value = "查找用户", notes = "根据用户名或昵称查找用户")
    public Result<List<UserVO>> findByName(@RequestParam("name") String name) {
        return ResultUtils.success(userService.findUserByName(name));
    }
}

