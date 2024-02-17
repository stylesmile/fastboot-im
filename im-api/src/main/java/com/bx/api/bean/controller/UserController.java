package com.bx.api.bean.controller;

import com.bx.api.bean.service.SessionService;
import com.bx.api.bean.service.UserService;
import com.bx.api.common.result.Result;
import com.bx.api.common.result.ResultUtils;
import com.bx.api.common.util.BeanUtils;
import com.bx.api.domain.dto.session.UserSession;
import com.bx.api.domain.entity.User;
import com.bx.api.domain.vo.OnlineTerminalVO;
import com.bx.api.domain.vo.UserVO;
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
    private UserService userService;
    @AutoWired
    private SessionService sessionService;

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
        UserSession session = sessionService.getSession();
        ;
        User user = userService.getById(session.getUserId());
        UserVO userVO = BeanUtils.copyProperties(user, UserVO.class);
        return ResultUtils.success(userVO);
    }


    //    @GetMapping("/user/find/{id}")
    @RequestMapping("/user/find")
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

