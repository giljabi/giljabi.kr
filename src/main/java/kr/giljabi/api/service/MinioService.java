package kr.giljabi.api.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import kr.giljabi.api.geo.JpegMetaInfo;
import kr.giljabi.api.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MinioService {

    @Value("${minio.bucketPublicUrl}")
    private String bucketPublicUrl;

    @Autowired
    private MinioClient minioClient;

    public String putObject(String bucketName, String objectName,
                           InputStream inputStream, String contentType) throws Exception {
        try (inputStream) {
            ObjectWriteResponse res = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build()
            );
            String result = "";
            if(bucketName.compareTo("gil-media-pri") == 0){
                result = String.format("%s/%s/%s", bucketPublicUrl, res.bucket(), res.object());
            } else if(bucketName.compareTo("gil-media-pub") == 0) {
                result = String.format("%s/%s/%s", bucketPublicUrl, res.bucket(), res.object());
            }

            return result;
        } catch (MinioException e) {
            log.error("Failed to save the compressed file to MinIO\n{}", e.toString());
            throw new MinioException("Failed to save the compressed file to MinIO", e.toString());
        }
    }

    public InputStream getObject(String bucketName, String objectUrl) throws IOException {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectUrl)
                            .build()
            );
            return stream;
        } catch (Exception e) {
            throw new IOException("Error reading file from MinIO", e);
        }
    }

    public JpegMetaInfo getMetaDataFromMinio(String bucketName, String objectName) throws Exception {
        String filaPath = objectName.substring(objectName.indexOf("/"));
        InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(filaPath).build());
        Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
        return CommonUtils.getMetaData(metadata);
    }

    /**
     * Object가 폴더인 경우가 있어서 폴더 내의 모든 Object를 삭제
     * @param bucketName
     * @param folderName
     * @throws Exception
     */
    public void deleteObject(String bucketName, String folderName) throws Exception {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName)
                            .prefix(folderName).recursive(false).build());
            boolean isDirectory = false;
            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.objectName().equals(folderName)) {
                    isDirectory = true;
                    break;
                }
            }
            if(isDirectory) {
                results = minioClient.listObjects(
                        ListObjectsArgs.builder().bucket(bucketName)
                                .prefix(folderName).recursive(true).build());
                for (Result<Item> result : results) {
                    Item item = result.get();
                    minioClient.removeObject(
                            RemoveObjectArgs.builder().bucket(bucketName)
                                    .object(item.objectName()).build());
                }
            } else {
                if(doesObjectExist(bucketName, folderName)) {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder().bucket(bucketName)
                                    .object(folderName).build());
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 1개 Object 삭제
     * @param bucketName
     * @param objectName
     * @throws Exception
     */
/*    public void deleteObject(String bucketName, String objectName) throws Exception {
        try {
            if(doesObjectExist(bucketName, objectName)) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
            }
        } catch (Exception e) {
            throw e;
        }
    }*/

    public boolean doesObjectExist(String bucketName, String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            return stat != null;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            return false;
        }
    }

    public String getUrl(String bucketName, String fileName) {
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

    /**
     * String 반환
     * @param bucketName
     * @param fileName
     * @return
     * @throws IOException
     */
    public String getObjectByString(String bucketName, String fileName) throws IOException {
        try (var stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build());
             var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            return fileContent.toString();
        } catch (Exception e) {
            throw new IOException("Error reading file from MinIO", e);
        }
    }

    /**
     * String을 List로 반환
     * @param bucketName
     * @param fileName
     * @return
     * @throws IOException
     */
    public List<String> getObjectStringByList(String bucketName, String fileName) throws IOException {
        List<String> lines = new ArrayList<>();

        try (var stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build());
             var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            throw new IOException("Error reading file from MinIO", e);
        }

        return lines;
    }

    /**
     * prefix로 시작하는 파일 목록 반환
     * @param bucketName
     * @param directory
     * @param prefix
     * @return
     */
    public List<String> listFiles(String bucketName, String directory, String prefix, String extension) {
        List<String> fileList = new ArrayList<>();
        String regex = prefix.replace("*", ".*");
        //gariwangsan*\\.gpx$
        String regexPattern = directory + regex + "\\." + extension+ "$";
        Pattern pattern = Pattern.compile(regexPattern);
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(directory)
                            .recursive(false)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (pattern.matcher(item.objectName()).matches()) {
                    fileList.add(item.objectName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error listing files from MinIO", e);
        }
        log.info("fileList: {}", fileList);
        return fileList;
    }


    public InputStream getFileInputStream(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

}