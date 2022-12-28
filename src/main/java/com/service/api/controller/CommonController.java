package com.service.api.controller;

import com.service.api.form.EmailForm;
import com.service.api.response.ApiResponse;
import com.service.config.Translator;
import com.service.dto.FileDTO;
import com.service.service.MailService;
import com.service.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;

import static com.service.util.ApiConst.API_VERSION_1;

@RestController
@RequestMapping("/commons")
@Tag(name = "Common Controller")
@Slf4j
public class CommonController {
    @Autowired
    private UploadService uploadService;
    @Autowired
    private MailService mailService;

    @Operation(description = "Upload file")
    @PostMapping(path = "/upload", headers = API_VERSION_1)
    public ApiResponse uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Request api POST api/v1/commons/upload");

        FileDTO fileDTO = uploadService.uploadFile(file);
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("file-upload-success"), fileDTO);
    }

    @Operation(description = "Send email to someone")
    @PostMapping(path = "/send-email", headers = API_VERSION_1)
    public ApiResponse sendEmail(@RequestBody EmailForm form) throws MessagingException {
        log.info("Request api POST api/v1/commons/send-email");

        mailService.sendEmail(form.getFrom(), form.getTo(), form.getSubject(), form.getBody(), form.getFile());
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("file-upload-success"));
    }
}
