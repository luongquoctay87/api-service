package com.example.sample.api.controller;

import com.example.sample.api.form.EmailForm;
import com.example.sample.api.response.ApiResponse;
import com.example.sample.config.Translator;
import com.example.sample.dto.FileDTO;
import com.example.sample.service.MailService;
import com.example.sample.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;

import static com.example.sample.util.ApiConst.API_VERSION_1;

@RestController
@RequestMapping("/commons")
@Slf4j
public class CommonController {

    @Autowired
    private UploadService uploadService;
    @Autowired
    private MailService mailService;

    @PostMapping(path = "/upload", headers = API_VERSION_1)
    public ApiResponse uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Request api POST api/v1/commons/upload");

        FileDTO fileDTO = uploadService.uploadFile(file);
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("file-upload-success"), fileDTO);
    }

    @PostMapping(path = "/send-email", headers = API_VERSION_1)
    public ApiResponse sendEmail(@RequestBody EmailForm form) throws MessagingException {
        log.info("Request api POST api/v1/commons/send-email");

        mailService.sendEmail(form.getFrom(), form.getTo(), form.getSubject(), form.getBody(), form.getFile());
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("file-upload-success"));
    }
}
