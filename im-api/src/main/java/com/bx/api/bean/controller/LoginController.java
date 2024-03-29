package com.bx.api.bean.controller;

import com.bx.api.bean.service.UserService;
import com.bx.api.common.result.Result;
import com.bx.api.common.result.ResultUtils;
import com.bx.api.domain.dto.LoginDTO;
import com.bx.api.domain.dto.ModifyPwdDTO;
import com.bx.api.domain.dto.RegisterDTO;
import com.bx.api.domain.vo.LoginVO;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Controller;
import io.github.stylesmile.annotation.RequestBody;
import io.github.stylesmile.annotation.RequestMapping;
import io.github.stylesmile.request.RequestMethod;
import io.github.stylesmile.server.Request;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.validation.Valid;

@Api(tags = "用户登录和注册")
@Controller
public class LoginController {
    @AutoWired

    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "用户登录", notes = "用户登录")
    public Result<LoginVO> login(@Valid LoginDTO dto, Request request) {
        return userService.login(dto, request);
    }


    @RequestMapping(value = "/refreshToken", method = RequestMethod.PUT)
    @ApiOperation(value = "刷新token", notes = "用refreshtoken换取新的token")
    public Result refreshToken(Request re) {
        String refreshToken = re.getHeaders().get("refreshToken");
        LoginVO vo = userService.refreshToken(refreshToken, re);
        return ResultUtils.success(vo);
    }


    //    @PostMapping("/register")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ApiOperation(value = "用户注册", notes = "用户注册")
    public Result register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return ResultUtils.success();
    }

    @RequestMapping(value = "/modifyPwd", method = RequestMethod.PUT)
    @ApiOperation(value = "修改密码", notes = "修改用户密码")
    public Result modifyPassword(@Valid @RequestBody ModifyPwdDTO dto) {
        userService.modifyPassword(dto);
        return ResultUtils.success();
    }

}
