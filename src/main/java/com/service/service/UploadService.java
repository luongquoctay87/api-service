package com.service.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.service.config.Translator;
import com.service.dto.FileDTO;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class UploadService {
    @Value("${cloud.aws.credentials.bucketName}")
    private String bucketName;

    @Autowired
    private AmazonS3Client amazonS3Client;

    public FileDTO uploadFile(MultipartFile file){
        String key = generateFileName(file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try{
            amazonS3Client.putObject(bucketName, key, file.getInputStream(),metadata);
        }catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Translator.toLocale("file-upload-fail"));
        }
        amazonS3Client.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
        FileDTO fileDTO = new FileDTO();
        fileDTO.setName(key);
        fileDTO.setUrl(amazonS3Client.getResourceUrl(bucketName, key));
        return fileDTO;
    }
    private String generateFileName(MultipartFile multiPart) {
        return new LocalDate() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }

}
