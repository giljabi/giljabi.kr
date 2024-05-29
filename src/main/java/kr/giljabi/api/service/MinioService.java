package kr.giljabi.api.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import kr.giljabi.api.geo.JpegMetaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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

    public JpegMetaInfo getMetaData(String bucketName, String objectName) throws Exception {
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