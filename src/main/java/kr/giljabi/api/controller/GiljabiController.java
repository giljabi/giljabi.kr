package kr.giljabi.api.controller;

import kr.giljabi.api.entity.GiljabiGpsdataImage;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.geo.JpegMetaInfo;
import kr.giljabi.api.request.RequestGpsDataDTO;
import kr.giljabi.api.response.GiljabiResponse;
import kr.giljabi.api.response.Response;
import kr.giljabi.api.service.GiljabiService;
import kr.giljabi.api.service.MinioService;
import kr.giljabi.api.utils.CommonUtils;
import kr.giljabi.api.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import com.github.diogoduailibe.lzstring4j.LZString;

import java.util.Optional;

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

    private final GiljabiService giljabiService;

    private final MinioService minioService;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.url}")
    private String s3url;

    //bucketName: service
    //file pth: service/yyyyMM/uuid_filename
    @PostMapping("/api/1.0/saveGpsdata")
    public Response gpsSave(final @Valid @RequestBody RequestGpsDataDTO gpsDataDTO) {
        try {
            String xmlData = LZString.decompressFromUTF16(gpsDataDTO.getXmldata());

            String filename = String.format("%s/%s.%s",
                    getFileLocation(gpsDataDTO.getUuid()), gpsDataDTO.getUuid(),
                    gpsDataDTO.getFileext());

            //압축된 상태로 저장하는것이 좋을까?
            String savedFilename = minioService.saveFile(bucketName, filename, gpsDataDTO.getXmldata());

            //db에 저장하는 코드
            GiljabiGpsdata gpsdata = new GiljabiGpsdata();
            gpsdata.setDistance(gpsDataDTO.getDistance());
            gpsdata.setFileext(gpsDataDTO.getFileext());
            gpsdata.setFileurl(savedFilename);
            //gpsdata.setSavedatetime(LocalDateTime.now());
            gpsdata.setSpeed(gpsDataDTO.getSpeed());
            gpsdata.setTrackname(gpsDataDTO.getTrackName());
            gpsdata.setTrkpt(gpsDataDTO.getTrkpt());
            gpsdata.setUser("sonnim");
            gpsdata.setUuid(gpsDataDTO.getUuid()); //filename
            gpsdata.setWpt(gpsDataDTO.getWpt());

            log.info("savedFilename: " + savedFilename);

            giljabiService.saveGpsdata(gpsdata);

            GiljabiResponse giljabiResponse = new GiljabiResponse();
            giljabiResponse.setFileKey(gpsDataDTO.getUuid());
            giljabiResponse.setFilePath(savedFilename);
            return new Response(giljabiResponse);
        }  catch (Exception e) {
            e.printStackTrace();
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

    @PostMapping("/api/1.0/uploadImage")
    public Response handleFileUpload(@RequestParam("file") MultipartFile file,
                                     @RequestParam("uuid") String uuidKey) {
        try {
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

            String filename = String.format("%s/%s",
                    getFileLocation(uuidKey),
                    CommonUtils.generateUUIDFilename(extension));
            String imageUrl = minioService.uploadFileImage(bucketName, filename, file);
            log.info("imageUrl: " + imageUrl);

            GiljabiGpsdata gpsdata = giljabiService.findByGpsdataUuid(uuidKey);

            JpegMetaInfo metadata = minioService.getMetaData(bucketName, imageUrl);

            //db에 저장하는 코드
            GiljabiGpsdataImage gpsImage = new GiljabiGpsdataImage();
            gpsImage.setFileext(extension.substring(extension.indexOf(".") + 1));
            gpsImage.setFileurl(s3url + "/" + imageUrl); //서버는 항상 다를 수 있음
            gpsImage.setGpsdata(gpsdata);
            gpsImage.setEle(metadata.getAltitude());
            gpsImage.setLat(metadata.getGeoLocation().getLatitude());
            gpsImage.setLng(metadata.getGeoLocation().getLongitude());
            gpsImage.setWidth(metadata.getImageWidth());
            gpsImage.setHeight(metadata.getImageLength());
            gpsImage.setMake(metadata.getMake());
            gpsImage.setModel(metadata.getModel());
            gpsImage.setOriginaldatetime(metadata.getDateTime());
            gpsImage.setOriginalfname(file.getOriginalFilename());
            giljabiService.saveGpsImage(gpsImage, gpsdata);

            GiljabiResponse giljabiResponse = new GiljabiResponse();
            giljabiResponse.setFilePath(s3url + "/" + imageUrl);
            giljabiResponse.setFileKey(uuidKey);
            giljabiResponse.setGeoLocation(metadata.getGeoLocation());
            giljabiResponse.setAltitude(metadata.getAltitude());
            giljabiResponse.setOriginalFileName(file.getOriginalFilename());

            return new Response(giljabiResponse);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

    @DeleteMapping("/api/1.0/deleteImage/{bucketName}/{yearmonth}/{uuidkey}/{filename:.+}")
    public Response deleteImage(@PathVariable String bucketName,
                                     @PathVariable String yearmonth,
                                     @PathVariable String uuidkey,
                                     @PathVariable String filename) {
        try {
            String objectPath = String.format("%s/%s/%s", yearmonth, uuidkey, filename);
            //파일을 삭제하기전 본인것인지....확인해야 함
            minioService.deleteObject(bucketName, objectPath);
            return new Response(ErrorCode.STATUS_SUCCESS.getStatus());
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }


    private String getFileLocation(String uuidKey) {
        return String.format("%s/%s", CommonUtils.getCurrentTime("YYYYMM"), uuidKey);
    }


}
