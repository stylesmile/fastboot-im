package com.bx.api.bean.controller;

import com.bx.api.config.ICEServer;
import com.bx.api.result.Result;
import com.bx.api.result.ResultUtils;
import com.bx.api.bean.service.WebrtcService;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Controller;
import io.github.stylesmile.annotation.RequestMapping;
import io.github.stylesmile.annotation.RequestParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

@Api(tags = "webrtc视频单人通话")
@Controller
public class WebrtcController {
    @AutoWired
    private WebrtcService webrtcService;

    @ApiOperation(httpMethod = "POST", value = "呼叫视频通话")
//    @PostMapping("/webrtc/private/call")
    @RequestMapping("/webrtc/private/call")
    public Result call(@RequestParam("uid") Long uid, String offer) {
        webrtcService.call(uid, offer);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "接受视频通话")
//    @PostMapping("/webrtc/private/accept")
    @RequestMapping("/webrtc/private/accept")
    public Result accept(@RequestParam("uid") Long uid, String answer) {
        webrtcService.accept(uid, answer);
        return ResultUtils.success();
    }


    @ApiOperation(httpMethod = "POST", value = "拒绝视频通话")
//    @PostMapping("/webrtc/private/reject")
    @RequestMapping("/webrtc/private/reject")
    public Result reject(@RequestParam("uid") Long uid) {
        webrtcService.reject(uid);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "取消呼叫")
//    @PostMapping("/webrtc/private/cancel")
    @RequestMapping("/webrtc/private/cancel")
    public Result cancel(@RequestParam("uid") Long uid) {
        webrtcService.cancel(uid);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "呼叫失败")
//    @PostMapping("/webrtc/private/failed")
    @RequestMapping("/webrtc/private/failed")
    public Result failed(@RequestParam("uid") Long uid, @RequestParam("reason") String reason) {
        webrtcService.failed(uid, reason);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "挂断")
//    @PostMapping("/webrtc/private/handup")
    @RequestMapping("/webrtc/private/handup")
    public Result leave(@RequestParam("uid") Long uid) {
        webrtcService.leave(uid);
        return ResultUtils.success();
    }


    //    @PostMapping("/webrtc/private/candidate")
    @RequestMapping("/webrtc/private/candidate")
    @ApiOperation(httpMethod = "POST", value = "同步candidate")
    public Result forwardCandidate(@RequestParam("uid") Long uid, String candidate) {
        webrtcService.candidate(uid, candidate);
        return ResultUtils.success();
    }


    //    @GetMapping("/webrtc/private/iceservers")
    @RequestMapping("/webrtc/private/iceservers")
    @ApiOperation(httpMethod = "GET", value = "获取iceservers")
    public Result<List<ICEServer>> iceservers() {
        return ResultUtils.success(webrtcService.getIceServers());
    }
}
