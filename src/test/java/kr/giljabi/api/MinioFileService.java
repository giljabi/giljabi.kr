package kr.giljabi.api;
import com.github.diogoduailibe.lzstring4j.LZString;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
public class MinioFileService {

    private MinioClient minioClient;

    // Constructor to initialize MinioClient
    public MinioFileService(String endpoint, String accessKey, String secretKey) {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public InputStream getFileInputStream(String bucketName, String objectName) throws Exception {
        // Get the file from the bucket
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
            System.out.println("Compressed Content Length: " + compressedContent.length());

            // Decompress the content using LZString
            String decompressedData = LZString.decompressFromUTF16(compressedContent);
            if (decompressedData == null) {
                throw new IOException("Decompression failed. The data may be corrupted or improperly formatted.");
            }

            return decompressedData;
        }
    }

    public void compressAndSaveFileToMinio(String localFilePath, String bucketName, String objectName) throws Exception {
        // Read the file from the local filesystem
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(localFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new IOException("Failed to read the local file", e);
        }

        // Compress the file content using LZString
        String originalContent = contentBuilder.toString();
        String compressedContent = LZString.compressToUTF16(originalContent);
        log.info("Compressed Content Length: " + compressedContent.length());
        // Save the compressed content to MinIO
        try (InputStream inputStream = new ByteArrayInputStream(compressedContent.getBytes(StandardCharsets.UTF_8))) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType("application/octet-stream")
                            .build()
            );
        } catch (MinioException e) {
            throw new MinioException("Failed to save the compressed file to MinIO", e.toString());
        }
    }

    public String decompressFile(String filename) throws Exception {
        try (InputStream inputStream = new FileInputStream(filename);
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
            System.out.println("Compressed Content Length: " + compressedContent.length());

            // Decompress the content using LZString
            String decompressedData = LZString.decompressFromUTF16(compressedContent);
            if (decompressedData == null) {
                throw new IOException("Decompression failed. The data may be corrupted or improperly formatted.");
            }

            return decompressedData;
        }
    }



    public static void main(String[] args) {
        try {

            String compressToBase64 = LZString.compressToBase64("giljabi.kr");
            System.out.println("Compressed to Base64: " + compressToBase64);
            System.out.println("Decompressed from Base64: " + LZString.decompressFromBase64(compressToBase64));

            // Initialize MinioFileService with your MinIO server details
            MinioFileService fileService = new MinioFileService("http://localhost:9000",
                    "Mvo42zZ1LmXcmsJ2l4wQ",
                    "JmLGYCAu8zBsWhzXsAb6QgYxJcI6CNSFsctxhOuB");

            fileService.decompressFile("/tmp/5a7559b2-0f24-4912-9e80-e1be66d39677.gpx");

            String uuid = UUID.randomUUID().toString();

            // Decompress the file from MinIO
            fileService.compressAndSaveFileToMinio("/tmp/activity_15680456571.gpx",
                    "service",
                    "/gpx/202406/activity_15680456571.gpx");
            String decompressedData = fileService.decompressFileFromMinio("service",
                    "/gpx/202406/activity_15680456571.gpx");

        } catch (MinioException e) {
            System.err.println("Minio error occurred: " + e);
        } catch (IOException e) {
            System.err.println("IO error occurred: " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
