package com.ql.BlogApplication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.UUID;

@Service

public class S3Service {

    @Value("${aws.accessKey}")
    String accessKey;

    @Value("${aws.secretKey}")
    String secretKey;

    @Value("${aws.region}")
    String region;

    @Value("${aws.s3.bucket}")
    String bucket;

//    public String uploadFile(MultipartFile multipartFile){
//
//         String fileName= UUID.randomUUID()+"_"+multipartFile.getOriginalFilename();
//         AwsBasicCredentials awsBasicCredentials=AwsBasicCredentials.create(accessKey,secretKey);
//
//         S3Client s3Client=S3Client.builder()
//                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
//                 .withRegion(Regions.us_east_1)
//                 .build();
//
//
//    }

}
