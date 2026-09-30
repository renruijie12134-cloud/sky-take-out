package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口，大家都可以用
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags="通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     * SpringMVC已经完成类型转换，直接使用MultipartFile file接收上传的文件
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> up(MultipartFile file) {
        log.info("文件上传:{}",file);
        try{
            // 你上上传的文件可能有重名的现象，为了防止重名文件在oss中被覆盖
            // 你可以使用UUID生成一个随机的文件名，或者使用时间戳
            // 1:获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 2:获取原始文件名后缀  xxxx.png
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 3：构造新的文件名
            String newFilename = UUID.randomUUID().toString() + suffix;

            // 4:上传文件到oss
            String url = aliOssUtil.upload(file.getBytes(), newFilename);

            return Result.success(url);
        }catch (IOException e){
            log.info("文件上传失败{}",e);

        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

}
