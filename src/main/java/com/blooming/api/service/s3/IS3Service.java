package com.blooming.api.service.s3;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;


public interface IS3Service {
    String uploadFile(String groupFile, MultipartFile file) throws IOException;

    InputStream getFile(String key);

    String updateFile(String key, MultipartFile newFile) throws IOException;

}
