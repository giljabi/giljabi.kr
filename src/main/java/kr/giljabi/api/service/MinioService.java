package kr.giljabi.api.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.github.diogoduailibe.lzstring4j.LZString;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import kr.giljabi.api.geo.JpegMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Value("${minio.awsObjectUrl}")
    private String s3url;

    @Autowired
    private MinioClient minioClient;

    public String saveFileToMinio(String bucketName, String objectName, String compressedContent) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(compressedContent.getBytes(StandardCharsets.UTF_8))) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType("application/octet-stream")
                            .build()
            );
            return s3url + "/" + bucketName + "/" + objectName;
        } catch (MinioException e) {
            log.error("Failed to save the compressed file to MinIO\n{}", e.toString());
            throw new MinioException("Failed to save the compressed file to MinIO", e.toString());
        }
    }

    public String saveFileToAws(String bucketName, String objectName, String compressedContent) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(compressedContent.getBytes(StandardCharsets.UTF_8))) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType("application/octet-stream")
                            .build()
            );
            return s3url + "/" +objectName;
        } catch (MinioException e) {
            log.error("Failed to save the compressed file to AWS S3\n{}", e.toString());
            throw new MinioException("Failed to save the compressed file to AWS", e.toString());
        }
    }

    public String saveImageFileToMinio(String bucketName, String pathAndFilename, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(pathAndFilename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return s3url + "/" + bucketName + "/" + pathAndFilename;
        } catch (Exception e) {
            log.error("Failed to save the compressed file to MinIO\n{}", e.toString());
            throw new RuntimeException("Error occurred while uploading file to MinIO", e);
        }
    }

    public String saveImageFileToAws(String bucketName, String pathAndFilename, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(pathAndFilename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return s3url + "/" +pathAndFilename;
        } catch (Exception e) {
            log.error("Failed to save the compressed file to MinIO\n{}", e.toString());
            throw new RuntimeException("Error occurred while uploading file to MinIO", e);
        }
    }

    public String saveTextFile(String bucketName, String filename, String data) {
        try (InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(inputStream, data.length(), -1)
                            .contentType("application/octet-stream")
                            .build());
            return bucketName + "/" + filename;

        } catch (Exception e) {
            throw new RuntimeException("Error occurred while uploading file to MinIO", e);
        }
    }
    /*
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

     */
    public JpegMetaInfo getMetaDataFromMinio(String bucketName, String objectName) throws Exception {
        String filaPath = objectName.substring(objectName.indexOf("/"));
        InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(filaPath).build());
        Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
        return getMetaData(metadata);
    }

    private JpegMetaInfo getMetaData(Metadata metadata) throws Exception {
        JpegMetaInfo jpegMetaInfo = new JpegMetaInfo();
        ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        jpegMetaInfo.setDateTime(exifIFD0Directory.getString(ExifIFD0Directory.TAG_DATETIME));
        jpegMetaInfo.setMake(exifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE));
        jpegMetaInfo.setModel(exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL));
        jpegMetaInfo.setOrientation(exifIFD0Directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION));

        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        jpegMetaInfo.setImageWidth(jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_WIDTH));
        jpegMetaInfo.setImageLength(jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT));

        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        jpegMetaInfo.setExifVersion(exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_EXIF_VERSION));

        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        jpegMetaInfo.setAltitude(gpsDirectory.getDouble(GpsDirectory.TAG_ALTITUDE));
        jpegMetaInfo.setGeoLocation(gpsDirectory.getGeoLocation());

        return jpegMetaInfo;
    }

    /**
     * 로그인이 있으면 본인것만 지울 수 있어야 함
     * @param bucketName
     * @param objectName
     * @throws Exception
     */
    public void deleteObject(String bucketName, String objectName) throws Exception {
        try {
            if(doesObjectExist(bucketName, objectName)) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
            }
        } catch (Exception e) {
            throw e;
        }
    }
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
    public String readFileContentByString(String bucketName, String fileName) throws IOException {
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
    public List<String> readFileContentByList(String bucketName, String fileName) throws IOException {
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

    public String decompressFileFromMinio(String bucketName, String objectName) throws Exception {
        try (InputStream inputStream = getFileInputStream(bucketName, objectName);
             InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {

            // Read the entire content into a StringBuilder
            StringBuilder contentBuilder = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                contentBuilder.append(buffer, 0, bytesRead);
            }
            String compressedContent = contentBuilder.toString();
            log.info("Compressed Content Length: " + compressedContent.length());

            // Debug: Print the compressed content length and a portion of the content
            log.info("Compressed Content Length: " + compressedContent.length());

            // Decompress the content using LZString
            String decompressedData = LZString.decompressFromUTF16(compressedContent);
            if (decompressedData == null) {
                throw new IOException("Decompression failed. The data may be corrupted or improperly formatted.");
            }

            return decompressedData;
        }
    }
}