package kr.giljabi.api.controller;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import io.swagger.annotations.ApiOperation;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.geo.Geometry3DPoint;
import kr.giljabi.api.geo.JpegMetaInfo;
import kr.giljabi.api.request.RequestGpsDataDTO;
import kr.giljabi.api.request.RequestRouteData;
import kr.giljabi.api.response.Response;
import kr.giljabi.api.service.GiljabiService;
import kr.giljabi.api.service.MinioService;
import kr.giljabi.api.service.RouteService;
import kr.giljabi.api.utils.CommonUtils;
import kr.giljabi.api.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import com.github.diogoduailibe.lzstring4j.LZString;

/**
 * Open Route Service를 이용한 경로탐색
 * @author eahn.park@gmail.com
 * 2018.10.26 gpx to tcx Project start....
 * 2021.01.10 openrouteapi license 변경
 * 2021.09.17 Spring boot 2.5.4
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class GiljabiController {

    //private final GiljabiService giljabiService;

    private final MinioService minioService;

//    @Value("${giljabi.image.physicalPath}")
//    private String physicalPath;

    @PostMapping("/api/1.0/gpsSave")
    public Response gpsSave(final @Valid @RequestBody RequestGpsDataDTO gpsDataDTO) {
        String xmlData = LZString.decompressFromUTF16(gpsDataDTO.getXmldata());
        //minioService.uploadFile(xmlData, gpsDataDTO.getFilename() + "." + gpsDataDTO.getFileext());

        gpsDataDTO.setXmldata(xmlData);
        //giljabiService.save();
        log.info(gpsDataDTO.toString());
        return new Response(ErrorCode.STATUS_SUCCESS.getStatus(), "Files uploaded successfully.");
    }

    @PostMapping("/api/1.0/imageUpload")
    public Response handleFileUpload(@RequestParam("file") MultipartFile file,
                                     @RequestParam("uuid") String uuidKey) {
        try {
            String bucketName = "images";
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String filename = String.format("%s/%s/%s",
                    CommonUtils.getCurrentTime("YYYYMM"),
                    uuidKey,
                    CommonUtils.generateUUIDFilename(extension));
            String imageUrl = minioService.uploadFileImage(bucketName, filename, file);
            log.info("imageUrl: " + imageUrl);
//            Metadata metadata = getMetaData(filePath.toFile());
//
//            GiljabiGpsdata gpsdata = new GiljabiGpsdata();
//            gpsdata.setUuid(uuid);
//            gpsdata.setFilename(file.getOriginalFilename());
//            gpsdata.setFileext("jpg");
//            gpsdata.setGpxname(file.getOriginalFilename());
//            gpsdata.setWpt(0);
//            gpsdata.setTrkpt(0);
//            gpsdata.setSpeed(0);
//            gpsdata.setDistance(0);

            //db에 저장하는 코드

            return new Response(ErrorCode.STATUS_SUCCESS.getStatus(),
                    "Files uploaded successfully.");
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

    private Metadata getMetaData(File file) throws Exception {
        Metadata metadata = ImageMetadataReader.readMetadata(file);

        JpegMetaInfo jpegMetaInfo = new JpegMetaInfo();

        ExifIFD0Directory  exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
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

        log.info(jpegMetaInfo.toString());
        return metadata;
    }

}
