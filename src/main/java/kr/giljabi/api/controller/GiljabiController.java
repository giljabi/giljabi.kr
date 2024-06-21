package kr.giljabi.api.controller;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import kr.giljabi.api.entity.GiljabiGpsdataImage;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.geo.JpegMetaInfo;
import kr.giljabi.api.request.RequestGpsDataDTO;
import kr.giljabi.api.response.GiljabiResponse;
import kr.giljabi.api.response.GiljabiResponseGpsdataDTO;
import kr.giljabi.api.response.GiljabiResponseGpsdataImageDTO;
import kr.giljabi.api.response.Response;
import kr.giljabi.api.service.GiljabiGpsDataImageService;
import kr.giljabi.api.service.GiljabiGpsDataService;
import kr.giljabi.api.service.MinioService;
import kr.giljabi.api.utils.CommonUtils;
import kr.giljabi.api.utils.ErrorCode;
import kr.giljabi.api.utils.MyHttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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

    private final GiljabiGpsDataService gpsService;
    private final GiljabiGpsDataImageService imageService;

    private final MinioService minioService;

    private UserInfo userInfo;

//    @Value("${minio.bucketPrivate}")
//    private String bucketPrivate;

    @Value("${minio.bucketPublic}")
    private String bucketPublic;

    @Value("${giljabi.gpx.path}")
    private String gpxPath;

    //bucketName: service
    //file pth: service/yyyyMM/uuid_filename
    @PostMapping("/api/1.0/saveGpsdata")
    public Response gpsSave(HttpServletRequest request,
                            final @Valid @RequestBody RequestGpsDataDTO gpsDataDTO) {
        try {
            log.info("gpsDataDTO.getXmldata().length: " + gpsDataDTO.getXmldata().length());
            userInfo = CommonUtils.getSessionByUserinfo(request);
            String compressedXml = gpsDataDTO.getXmldata();

            String filename = CommonUtils.makeGpsdataObjectName(gpxPath,
                    gpsDataDTO.getUuid(),
                    gpsDataDTO.getFileext());

            InputStream inputStream = new ByteArrayInputStream(compressedXml.getBytes(StandardCharsets.UTF_8));
            String savedFilename = minioService.putObject(bucketPublic,
                    filename, inputStream, CommonUtils.BINARY_CONTENT_TYPE);

            GiljabiGpsdata gpsdata = CommonUtils.makeGiljabiGpsdata(
                    MyHttpUtils.getClientIp(request),
                    "saveGpsdata",
                    gpsDataDTO,
                    0,//compressed
                    compressedXml.getBytes().length,
                    savedFilename,
                    userInfo.getUserid());
            log.info("saveGpsdata: " + savedFilename);
            gpsService.saveGpsdata(gpsdata);

            GiljabiResponse giljabiResponse = new GiljabiResponse();
            giljabiResponse.setFileKey(gpsDataDTO.getUuid());
            giljabiResponse.setFilePath(savedFilename);
            return new Response(giljabiResponse);
        }  catch (Exception e) {
            e.printStackTrace();
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

    /**
     * bucketName: service
     * path: service/gpx/yyyyMM/uuid_filename/uuid_filename.jpg
     * @param file
     * @param uuidKey
     * @return
     */
    @PostMapping("/api/1.0/uploadImage")
    public Response uploadImage(HttpServletRequest request,
                                     @RequestParam("file") MultipartFile file,
                                     @RequestParam("uuid") String uuidKey) {
        try {
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String filename = String.format("%s/%s/%s",
                    gpxPath,
                    CommonUtils.getFileLocation(uuidKey),
                    CommonUtils.generateUUIDFilename(extension));

            String savedFilename = minioService.putObject(bucketPublic,
                    filename, file.getInputStream(), CommonUtils.BINARY_CONTENT_TYPE);

            JpegMetaInfo metadata = CommonUtils.getMetaData(ImageMetadataReader.readMetadata(file.getInputStream()));
            log.info("metadata: " + metadata.toString());

            GiljabiGpsdata gpsdata = gpsService.findByUuid(uuidKey);
            //db에 저장하는 코드
            GiljabiGpsdataImage gpsImage = new GiljabiGpsdataImage();
            gpsImage.setFileext(extension.substring(extension.indexOf(".") + 1));
            gpsImage.setFileurl(savedFilename); //서버는 항상 다를 수 있음
            gpsImage.setGpsdata(gpsdata);   //gpsdata에 대한 참조 키
            gpsImage.setEle(metadata.getAltitude());
            gpsImage.setLat(metadata.getGeoLocation().getLatitude());
            gpsImage.setLng(metadata.getGeoLocation().getLongitude());
            gpsImage.setWidth(metadata.getImageWidth());
            gpsImage.setHeight(metadata.getImageLength());
            gpsImage.setMake(metadata.getMake());
            gpsImage.setModel(metadata.getModel());
            gpsImage.setOriginaldatetime(metadata.getDateTime());
            gpsImage.setOriginalfname(file.getOriginalFilename());
            gpsImage.setFilesize(file.getSize());
            gpsImage.setUserip(MyHttpUtils.getClientIp(request));
            imageService.saveGpsImage(gpsImage, gpsdata);

            GiljabiResponse giljabiResponse = new GiljabiResponse();
            giljabiResponse.setFilePath(savedFilename);
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

    @GetMapping("/api/1.0/getImageList/{uuidkey}")
    public Response getImageList(@PathVariable String uuidkey) {
        try {
            GiljabiGpsdata gpsdata = gpsService.findByUuid(uuidkey);
            //GiljabiGpsdata에 addGpsImage을 사용하는 경우는 조인되는 테이블이 많고 중첩되면 쿼리가 매우 위험해질 수 있음
            //이런 경우 addGpsImage을         //gpsdata.addGpsImage(gpsImage); 사용하는 것도 검토할 수 있음
            //ArrayList<GiljabiGpsdataImage> images = imageService.findAllByGpsdata(gpsdata);

            GiljabiResponseGpsdataDTO dto = new GiljabiResponseGpsdataDTO();
            dto.setFileurl(gpsdata.getFileurl());
            dto.setTrackname(gpsdata.getTrackname());
            dto.setUuid(gpsdata.getUuid());
            dto.setId((int)gpsdata.getId());
            ArrayList<GiljabiResponseGpsdataImageDTO> imagesDTOList = new ArrayList<>();
            gpsdata.getGpsdataimages().forEach(gpsImage -> {
                GiljabiResponseGpsdataImageDTO gpsImageDTO = new GiljabiResponseGpsdataImageDTO();
                gpsImageDTO.setId(gpsImage.getId());
                GeoLocation geoLocation = new GeoLocation(gpsImage.getLat(), gpsImage.getLng());
                gpsImageDTO.setGeoLocation(geoLocation);
                gpsImageDTO.setAltitude(gpsImage.getEle());
                gpsImageDTO.setOriginaldatetime(gpsImage.getOriginaldatetime());
                gpsImageDTO.setEle(gpsImage.getEle());
                gpsImageDTO.setFileurl(gpsImage.getFileurl());
                gpsImageDTO.setOriginalfname(gpsImage.getOriginalfname());
                imagesDTOList.add(gpsImageDTO);
            });
            dto.setGpsdataimages(imagesDTOList);
            return new Response(dto);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

    /**
     * apiname editor에서 연결해서 가져오는 경우에 사용됨: linkElevation
     * action: linkElevation,
     * @param uuidkey
     * @return
     */
    @GetMapping("/api/1.0/getByParameter/{uuidkey}/{action}")
    public Response getByParameter(@PathVariable String uuidkey,
                                   @PathVariable String action) {
        try {
            GiljabiGpsdata gpsdata = null;
            if(action.compareToIgnoreCase("linkElevation") == 0) {
                gpsdata = gpsService.findByApinameAndUuidAndCreateat(uuidkey);
            } else {
                gpsdata = gpsService.findByUuid(uuidkey); //shareflag = true
            }

            if(gpsdata == null) {
                return new Response(ErrorCode.STATUS_FAILURE.getStatus(), "Not found data");
            }
            int index = gpsdata.getFileurl().indexOf(bucketPublic);
            String filePath = gpsdata.getFileurl().substring(index);

            String reader = minioService.getObjectByString(bucketPublic,
                    filePath.substring(filePath.indexOf("/") + 1));

            //elevation --> giljabi 임시사용이므로 삭제
            if(action.compareToIgnoreCase("linkElevation") == 0)
                minioService.deleteObject(bucketPublic,
                    filePath.substring(filePath.indexOf("/") + 1,
                            filePath.lastIndexOf("/")));

            GiljabiResponseGpsdataDTO dto = new GiljabiResponseGpsdataDTO();
            dto.setXmldata(reader);
            dto.setFileext(gpsdata.getFileext());
            dto.setFileurl(gpsdata.getFileurl());
            dto.setTrackname(gpsdata.getTrackname());
            dto.setUuid(gpsdata.getUuid());
            return new Response(dto);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

    @GetMapping("/api/1.0/getOldShare/{uuidkey}")
    public Response getOldShare(@PathVariable String uuidkey) {
        try {
            GiljabiGpsdata gpsdata = gpsService.findByUuid(uuidkey);
            gpsService.findByUuidAndShareflagTrue(uuidkey);

            if(gpsdata == null) {
                return new Response(ErrorCode.STATUS_FAILURE.getStatus(), "Not found data");
            }
            int index = gpsdata.getFileurl().indexOf(bucketPublic);
            String filePath = gpsdata.getFileurl().substring(index);

            String reader = minioService.getObjectByString(bucketPublic,
                    filePath.substring(filePath.indexOf("/") + 1));

            GiljabiResponseGpsdataDTO dto = new GiljabiResponseGpsdataDTO();
            dto.setXmldata(reader);
            dto.setFileext(gpsdata.getFileext());
            dto.setFileurl(gpsdata.getFileurl());
            dto.setTrackname(gpsdata.getTrackname());
            dto.setUuid(gpsdata.getUuid());
            return new Response(dto);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

}
