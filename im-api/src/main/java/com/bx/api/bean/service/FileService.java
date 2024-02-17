package com.bx.api.bean.service;

import com.bx.api.common.contant.Constant;
import com.bx.api.common.enums.FileType;
import com.bx.api.common.enums.ResultCode;
import com.bx.api.common.exception.GlobalException;
import com.bx.api.common.util.FileUtil;
import com.bx.api.common.util.ImageUtil;
import com.bx.api.common.util.MinioUtil;
import com.bx.api.domain.vo.UploadImageVO;
import io.github.stylesmile.annotation.AutoWired;
import io.github.stylesmile.annotation.Service;
import io.github.stylesmile.file.UploadedFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 通过校验文件MD5实现重复文件秒传
 * 文件上传服务
 *
 * @author Blue
 * @date 2022/10/28
 */
@Slf4j
@Service
public class FileService {
    @AutoWired
    private MinioUtil minioUtil;
    @Value("${minio.public}")
    private String minIoServer;
    @Value("${minio.bucketName}")
    private String bucketName;
    @Value("${minio.imagePath}")
    private String imagePath;
    @Value("${minio.filePath}")
    private String filePath;
    @Value("${minio.videoPath}")
    private String videoPath;

    @AutoWired
    private SessionService sessionService;
    @PostConstruct
    public void init() {
        if (!minioUtil.bucketExists(bucketName)) {
            // 创建bucket
            minioUtil.makeBucket(bucketName);
            // 公开bucket
            minioUtil.setBucketPublic(bucketName);
        }
    }


    public String uploadFile(UploadedFile file) {
        long userId = sessionService.getSession().getUserId();

        // 大小校验
        if (file.getContentSize() > Constant.MAX_FILE_SIZE) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件大小不能超过10M");
        }
        // 上传
        String fileName = minioUtil.upload(bucketName, filePath, file);
        if (StringUtils.isEmpty(fileName)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件上传失败");
        }
        String url = generUrl(FileType.FILE, fileName);
        log.info("文件文件成功，用户id:{},url:{}", userId, url);
        return url;
    }

    public UploadImageVO uploadImage(UploadedFile file) {
        try {
            Long userId = sessionService.getSession().getUserId();
            // 大小校验
            if (file.getContentSize() > Constant.MAX_IMAGE_SIZE) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片大小不能超过5M");
            }
            // 图片格式校验
            if (!FileUtil.isImage(file.getName())) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片格式不合法");
            }
            // 上传原图
            UploadImageVO vo = new UploadImageVO();
            String fileName = minioUtil.upload(bucketName, imagePath, file);
            if (StringUtils.isEmpty(fileName)) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
            }
            vo.setOriginUrl(generUrl(FileType.IMAGE, fileName));
            // 大于30K的文件需上传缩略图
            if (file.getContentSize() > 30 * 1024) {
                byte[] byteArray = null;
                try (InputStream inputStream = file.getContent();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    byteArray = outputStream.toByteArray();
                    System.out.println("字节数组长度： " + byteArray.length);
                }

                byte[] imageByte = ImageUtil.compressForScale(byteArray, 30);

                fileName = minioUtil.upload(bucketName, imagePath, Objects.requireNonNull(fileName), imageByte, file.getContentType());
                if (StringUtils.isEmpty(fileName)) {
                    throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
                }
            }
            vo.setThumbUrl(generUrl(FileType.IMAGE, fileName));
            log.info("文件图片成功，用户id:{},url:{}", userId, vo.getOriginUrl());
            return vo;
        } catch (IOException e) {
            log.error("上传图片失败，{}", e.getMessage(), e);
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
        }
    }


    public String generUrl(FileType fileTypeEnum, String fileName) {
        String url = minIoServer + "/" + bucketName;
        switch (fileTypeEnum) {
            case FILE:
                url += "/" + filePath + "/";
                break;
            case IMAGE:
                url += "/" + imagePath + "/";
                break;
            case VIDEO:
                url += "/" + videoPath + "/";
                break;
            default:
                break;
        }
        url += fileName;
        return url;
    }

}
