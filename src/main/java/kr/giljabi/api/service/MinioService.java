package kr.giljabi.api.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    public void uploadFile(String bucketName, String data, String filename) {
        try (InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(inputStream, data.length(), -1)
                            .contentType("application/octet-stream")
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while uploading file to MinIO", e);
        }
    }

    public String uploadFileImage(String bucketName, String pathAndFilename, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(pathAndFilename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return bucketName + "/" + pathAndFilename;
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while uploading file to MinIO", e);
        }
    }

    private String getUrl(String bucketName, String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            //.expiry(60 * 60 * 24) // 24 hours
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while generating file URL", e);
        }
    }

}