package kr.giljabi.minio;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinioCheckObject {
    public static void main(String[] args) {
        try {
            // MinIO 클라이언트 초기화
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("http://localhost:9000")
                    .credentials("Mvo42zZ1LmXcmsJ2l4wQ",
                            "JmLGYCAu8zBsWhzXsAb6QgYxJcI6CNSFsctxhOuB")
                    .build();

            // 버킷 이름과 객체 이름 설정
            String bucketName = "gil-media-pub";
            String objectName = "gpx/202406/10e7d619-81d4-42d7-a1f8-4a0ddef40a47/10e7d619-81d4-42d7-a1f8-4a0ddef40a47.tcx";

            // 객체가 존재하는지 확인
            boolean found = minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build()) != null;
            if (found) {
                System.out.println("Object exists.");
            } else {
                System.out.println("Object does not exist.");
            }

        } catch (MinioException e) {
            System.err.println("Error occurred: " + e);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}