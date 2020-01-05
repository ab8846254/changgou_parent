package com.changgou.controller;

import entity.IdWorker;
import entity.QiniuUtils;

import entity.Result;
import entity.StatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {
    /***
     * 上传
     * @param file
     * @return
     * @throws IOException
     */

    @PostMapping("/file")
    public Result upload(@RequestParam(value = "file") MultipartFile file) throws IOException {
        IdWorker idWorker = new IdWorker();
        long fileName = idWorker.nextId();
        String originalFilename = file.getOriginalFilename();
        originalFilename = fileName + "";
        QiniuUtils.upload2Qiniu(file.getBytes(), originalFilename + ".jpg");
        String url = "http://q30asv7kq.bkt.clouddn.com/" + originalFilename + ".jpg";
        return new Result(false, StatusCode.OK, "文件上传成功", url);

    }

}
