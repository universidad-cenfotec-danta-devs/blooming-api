package com.blooming.api.service.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class S3Service implements IS3Service {
    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(String groupFile, MultipartFile file) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        String key = groupFile + "/" + UUID.randomUUID() + "/" + file.getOriginalFilename();

        InputStream inputStream = file.getInputStream();
        try {
            s3Client.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
        return s3Client.getUrl(bucketName, key).toString();
    }

    @Override
    public InputStream getFile(String key) {
        S3Object s3Object = s3Client.getObject(bucketName, key);
        return s3Object.getObjectContent();
    }

    @Override
    public String updateFile(String key, MultipartFile newFile) throws IOException {
        deleteFile(key); // Delete the existing file first
        return uploadFile(key, newFile); // Upload the new file
    }

    private void deleteFile(String key) {
        s3Client.deleteObject(bucketName, key);
    }

}
