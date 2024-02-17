package com.bx.api.bean.controller;

import com.bx.api.common.result.Result;
import com.bx.api.common.result.ResultUtils;
import com.bx.api.bean.service.FileService;
import com.bx.api.domain.vo.UploadImageVO;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Controller;
import io.github.stylesmile.annotation.RequestMapping;
import io.github.stylesmile.file.UploadedFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@Api(tags = "文件上传")
//@RequiredArgsConstructor
public class FileController {

    @AutoWired
    private FileService fileService;

    @ApiOperation(value = "上传图片", notes = "上传图片,上传后返回原图和缩略图的url")
    @RequestMapping("/image/upload")
    public Result<UploadImageVO> uploadImage(UploadedFile file) {
        return ResultUtils.success(fileService.uploadImage(file));
    }

    @ApiOperation(value = "上传文件", notes = "上传文件，上传后返回文件url")
//    @PostMapping("/file/upload")
    @RequestMapping("/file/upload")
    public Result<String> uploadFile(UploadedFile file) {
        return ResultUtils.success(fileService.uploadFile(file), "");
    }

}
