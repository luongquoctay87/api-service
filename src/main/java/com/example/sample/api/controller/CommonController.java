package com.example.sample.api.controller;

import com.example.sample.api.response.ApiResponse;
import com.example.sample.config.Translator;
import com.example.sample.dto.FileDTO;
import com.example.sample.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/commons")
@Slf4j
public class CommonController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("/upload")
    public ApiResponse uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Request api POST api/v1/commons/upload");

        FileDTO fileDTO = uploadService.uploadFile(file);
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("file-upload-success"), fileDTO);
    }
}
