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
import kr.giljabi.api.response.GiljabiResponse;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Optional;

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

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.url}")
    private String s3url;

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
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String filename = String.format("%s/%s/%s",
                    CommonUtils.getCurrentTime("YYYYMM"),
                    uuidKey,
                    CommonUtils.generateUUIDFilename(extension));
            String imageUrl = minioService.uploadFileImage(bucketName, filename, file);
            log.info("imageUrl: " + imageUrl);

            JpegMetaInfo metadata = minioService.getMetaData(bucketName, imageUrl);

            GiljabiGpsdata gpsdata = new GiljabiGpsdata();
//            gpsdata.setUuid(uuidKey);
//            gpsdata.setFilename(file.getOriginalFilename());
//            gpsdata.setFileext(extension.substring(1));
//            gpsdata.setGpxname(file.getOriginalFilename());
//            gpsdata.setWpt(0);
//            gpsdata.setTrkpt(0);
//            gpsdata.setSpeed(0);
//            gpsdata.setDistance(0);

            //db에 저장하는 코드
            GiljabiResponse giljabiResponse = new GiljabiResponse();
            giljabiResponse.setFilePath(s3url + "/" + imageUrl);
            giljabiResponse.setFileKey(uuidKey);
            giljabiResponse.setGeoLocation(metadata.getGeoLocation());
            giljabiResponse.setAltitude(metadata.getAltitude());
            return new Response(giljabiResponse);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }




}
