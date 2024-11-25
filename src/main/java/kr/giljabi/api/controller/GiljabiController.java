package kr.giljabi.api.controller;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import io.swagger.v3.oas.annotations.Operation;
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
import kr.giljabi.api.service.JwtProviderService;
import kr.giljabi.api.utils.CommonUtils;
import kr.giljabi.api.utils.ErrorCode;
import kr.giljabi.api.utils.FileUtils;
import kr.giljabi.api.utils.MyHttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final JwtProviderService jwtProviderService;

    //private final MinioService minioService;

    private UserInfo userInfo;

/*    @Value("${minio.bucketPublic}")
    private String bucketPublic;*/

    @Value("${giljabi.gpx.path}")
    private String gpxPath;

    //bucketName: service
    //file pth: service/yyyyMM/uuid_filename
    //일반 사용자는 무조건 추가
    //관리자는 saveas/update 선택임
    @PostMapping("/api/1.0/saveGpsdata/{param}")
    @Operation(summary = "gpx파일 저장, 압축된 형태를 파일로 저장하고 db에 저장한다.")
    public Response gpsSave(HttpServletRequest request,
                            @PathVariable(required = false ) String param,
                            final @Valid @RequestBody RequestGpsDataDTO gpsDataDTO) {
        try {
            log.info("gpsDataDTO.getXmldata().length: " + gpsDataDTO.getXmldata().length());
            userInfo = jwtProviderService.getSessionByUserinfo(request);
            String compressedXml = gpsDataDTO.getXmldata();

            String logicalFileName = CommonUtils.makeGpsdataObjectPath(gpsDataDTO.getUuid());
            String savedFilename = FileUtils.saveFile(gpxPath + logicalFileName,
                    gpsDataDTO.getUuid(), compressedXml);
            log.info("saveGpsdata File save: " + savedFilename);
            GiljabiGpsdata gpsdata = CommonUtils.makeGiljabiGpsdata(
                    MyHttpUtils.getClientIp(request),
                    "saveGpsdata",
                    gpsDataDTO,
                    0, // compressed
                    compressedXml.getBytes().length,
                    logicalFileName,
                    userInfo.getUserid(),
                    gpsDataDTO.getUserUUID()
            );

            if(param.compareToIgnoreCase("update") == 0) {
                GiljabiGpsdata gpsdataCheck = gpsService.findByUuid(gpsDataDTO.getUuid());
                gpsdata.setId(gpsdataCheck.getId());    // null이면 insert
            }

            gpsService.save(gpsdata);

            GiljabiResponse giljabiResponse = new GiljabiResponse();
            giljabiResponse.setFileKey(gpsDataDTO.getUuid());
            giljabiResponse.setFilePath(logicalFileName);
            return new Response(giljabiResponse);
        }  catch (Exception e) {
            e.printStackTrace();
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }
/*
admin은 파일이 있으면 추가하지 않고 update
    //bucketName: service
    //file pth: service/yyyyMM/uuid_filename
    @PostMapping("/api/1.0/saveGpsdata")
    public Response gpsSave(HttpServletRequest request,
                            final @Valid @RequestBody RequestGpsDataDTO gpsDataDTO) {
        try {
            log.info("gpsDataDTO.getXmldata().length: " + gpsDataDTO.getXmldata().length());
            userInfo = jwtProviderService.getSessionByUserinfo(request);
            String compressedXml = gpsDataDTO.getXmldata();

            String logicalFileName = CommonUtils.makeGpsdataObjectPath(gpsDataDTO.getUuid());
            String savedFilename = FileUtils.saveFile(gpxPath + logicalFileName,
                    gpsDataDTO.getUuid(), compressedXml);
            log.info("saveGpsdata File save: " + savedFilename);
            GiljabiGpsdata gpsdata = CommonUtils.makeGiljabiGpsdata(
                    MyHttpUtils.getClientIp(request),
                    "saveGpsdata",
                    gpsDataDTO,
                    0, // compressed
                    compressedXml.getBytes().length,
                    logicalFileName,
                    userInfo.getUserid(),
                    gpsDataDTO.getUserUUID()
            );

            GiljabiGpsdata gpsdataCheck = gpsService.findByUuid(gpsDataDTO.getUuid());
            if(gpsdataCheck != null) { //insert
                gpsdata.setId(gpsdataCheck.getId());    // null이면 insert
                log.info("saveGpsdata Update: " + gpsDataDTO.getUuid());
            } else {
                log.info("saveGpsdata Insert: " + gpsDataDTO.getUuid());
            }
            gpsService.save(gpsdata);

            GiljabiResponse giljabiResponse = new GiljabiResponse();
            giljabiResponse.setFileKey(gpsDataDTO.getUuid());
            giljabiResponse.setFilePath(logicalFileName);
            return new Response(giljabiResponse);
        }  catch (Exception e) {
            e.printStackTrace();
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }
 */
    /**
     * bucketName: service
     * path: service/gpx/yyyyMM/uuid_filename/uuid_filename.jpg
     * @param file
     * @param parentUuid
     * @return
     */
    @PostMapping("/api/1.0/uploadImage")
    @Operation(summary = "관리자만 사용, gpx경로에 이미지를 추가")
    public Response uploadImage(HttpServletRequest request,
                                     @RequestParam("file") MultipartFile file,
                                     @RequestParam("uuid") String parentUuid) {
        try {
            userInfo = jwtProviderService.getSessionByUserinfo(request);
            if(Integer.parseInt(userInfo.getLevel()) < 10) {
                return new Response(ErrorCode.STATUS_FAILURE.getStatus(), "준비중인 기능입니다.");
            }

            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            if (file != null && !(extension.endsWith(".png") || extension.endsWith(".jpg") || extension.endsWith(".jpeg"))) {
                return new Response(ErrorCode.STATUS_FAILURE.getStatus(), "파일이 없거나, 지원하지 않는 파일 형식입니다.");
            }

            String imageFileName = CommonUtils.generateUUIDFilename(extension);
            String logicalFileName = CommonUtils.makeGpsdataObjectPath(parentUuid);
            String savedFilename = FileUtils.saveFile(gpxPath + logicalFileName,
                    imageFileName, file);

            //저장된 파일을 InputStream으로 읽어서 메타데이터를 추출
            InputStream inputStream = new FileInputStream(savedFilename);
            JpegMetaInfo metadata = CommonUtils.getMetaData(ImageMetadataReader.readMetadata(inputStream));
            inputStream.close();
            log.info("metadata: " + metadata.toString());

            GiljabiGpsdata gpsdata = gpsService.findByUuid(parentUuid);
            //db에 저장하는 코드
            GiljabiGpsdataImage gpsImage = new GiljabiGpsdataImage();
            gpsImage.setFileext(extension.substring(extension.indexOf(".") + 1));
            gpsImage.setFileurl(logicalFileName + "/" + imageFileName);
            gpsImage.setGpsdata(gpsdata);   //gpsdata에 대한 참조 키
            gpsImage.setEle(metadata.getAltitude());
            gpsImage.setLat(metadata.getGeoLocation().getLatitude());
            gpsImage.setLng(metadata.getGeoLocation().getLongitude());
            gpsImage.setWidth(metadata.getImageWidth());
            gpsImage.setHeight(metadata.getImageLength());
            gpsImage.setMake(metadata.getMake());
            gpsImage.setModel(metadata.getModel());
            gpsImage.setOriginaldatetime(metadata.getDateTime());
            gpsImage.setOriginalfname(file.getOriginalFilename());  //DB는 원 파일명을 저장
            gpsImage.setFilesize(file.getSize());
            gpsImage.setUserip(MyHttpUtils.getClientIp(request));
            imageService.save(gpsImage, gpsdata);


            String[] gpxPathArray = gpxPath.split("/");     //gpx 위치
            GiljabiResponse giljabiResponse = new GiljabiResponse();
            giljabiResponse.setImageId(gpsImage.getId());
            giljabiResponse.setFilePath("/" + gpxPathArray[gpxPathArray.length - 1] + gpsImage.getFileurl());
            giljabiResponse.setFileKey(parentUuid);
            giljabiResponse.setGeoLocation(metadata.getGeoLocation());
            giljabiResponse.setAltitude(metadata.getAltitude());
            String[] uuidKeyArray = imageFileName.split("-"); //화면에 보이는 파일명은 일관성을 유지
            giljabiResponse.setOriginalFileName(uuidKeyArray[uuidKeyArray.length - 1]);

            return new Response(giljabiResponse);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

    @DeleteMapping("/api/1.0/deleteImage/{imageId}")
    @Operation(summary = "관리자만 사용, 이미지 삭제")
    public Response deleteImage(HttpServletRequest request,
                                @PathVariable Long imageId) {
        try {
            //파일을 삭제하기전 본인것인지....확인해야 하는데, 관리자만 사용하자...
            userInfo = jwtProviderService.getSessionByUserinfo(request);
            if (Integer.parseInt(userInfo.getLevel()) < 10) {
                return new Response(ErrorCode.STATUS_FAILURE.getStatus(), "준비중인 기능입니다.");
            }

            //DB에서 파일정보를 가져온다
            GiljabiGpsdataImage gpsImage = imageService.findById(imageId);
            String physicalFilePath = gpxPath + gpsImage.getFileurl();
            System.out.println("physicalFilePath: " + physicalFilePath);

            //fileDeleteFlag 사용하지 않음
            boolean fileDeleteFlag = FileUtils.deleteFile(physicalFilePath);
            if (fileDeleteFlag) {
                imageService.deleteById(gpsImage.getId());
                return new Response(ErrorCode.STATUS_SUCCESS.getStatus());
            } else
                return new Response(ErrorCode.STATUS_FAILURE.getStatus(), "파일 삭제 실패");
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

    @GetMapping("/api/1.0/getImageList/{uuidkey}")
    @Operation(summary = "gpx정보에서 이미지가 있으면 화면에 보여줌")
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
     */
    /*
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
*/
    /*
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
*/
    @GetMapping("/api/1.0/getGpxList")
    @Operation(summary = "최근 저장된 gpx 목록")
    public Response getGpxList(HttpServletRequest request,
                               @RequestParam(required = false) String trackName,
                               @RequestParam(required = false) String useruuid,
                               @RequestParam(required = false) boolean selfCheck,
                               Pageable pageable) {
        try {
            Sort sort = Sort.by(Sort.Direction.DESC, "id");
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

            Page<GiljabiGpsdata> pageContents =
                    gpsService.findGpsDataBetweenDatesAndTrackName(
                            trackName, useruuid, selfCheck, pageable);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            List<GiljabiResponseGpsdataDTO> content = new ArrayList<>();
            for(GiljabiGpsdata gpsdata : pageContents.getContent()) {
                GiljabiResponseGpsdataDTO gpsdataDTO = new GiljabiResponseGpsdataDTO();
                gpsdataDTO.setApiname(gpsdata.getApiname());
                gpsdataDTO.setUserid(gpsdata.getUserid());
                gpsdataDTO.setCreateat(gpsdata.getCreateat().toLocalDateTime().format(formatter));
                gpsdataDTO.setDistance(gpsdata.getDistance());
                gpsdataDTO.setSpeed(gpsdata.getSpeed());
                gpsdataDTO.setTrkpt(gpsdata.getTrkpt());
                gpsdataDTO.setWpt(gpsdata.getWpt());
                gpsdataDTO.setFileurl(gpsdata.getFileurl());
                gpsdataDTO.setTrackname(gpsdata.getTrackname());
                gpsdataDTO.setUuid(gpsdata.getUuid());
                gpsdataDTO.setId((int)gpsdata.getId());
                gpsdataDTO.setFileext(gpsdata.getFileext());
                gpsdataDTO.setShareflag(gpsdata.isShareflag());
                if(gpsdata.getUseruuid() != null && gpsdata.getUseruuid().length() > 24)
                    gpsdataDTO.setUseruuid(gpsdata.getUseruuid().substring(24));
                else
                    gpsdataDTO.setUseruuid("");
                content.add(gpsdataDTO);
            }
            return new Response(Map.ofEntries(
                    Map.entry("content", content),
                    Map.entry("totalPages", pageContents.getTotalPages()),
                    Map.entry("totalElements", pageContents.getTotalElements()),
                    Map.entry("size", pageContents.getSize()),
                    Map.entry("number", pageContents.getNumber()),
                    Map.entry("numberOfElements", pageContents.getNumberOfElements()),
                    Map.entry("first", pageContents.isFirst()),
                    Map.entry("empty", pageContents.isEmpty()),
                    Map.entry("isFirst", pageContents.isFirst()),
                    Map.entry("isLast", pageContents.isLast())
            ));
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

    @PatchMapping("/api/1.0/changeGpx/{uuidkey}/{shareflag}")
    @Operation(summary = "관리자 사용, 현재 gpx를 장기 보관상태 변경")
    public Response changeShareFlag(HttpServletRequest request,
                                    @PathVariable String uuidkey,
                                    @PathVariable boolean shareflag) {
        try {
            userInfo = jwtProviderService.getSessionByUserinfo(request);
            if(Integer.parseInt(userInfo.getLevel()) < 10) {
                return new Response(ErrorCode.STATUS_FAILURE.getStatus(), "준비중인 기능입니다.");
            }

            if(gpsService.updateShareFlagByUuid(uuidkey, shareflag) > 0) {
                return new Response(ErrorCode.STATUS_SUCCESS.getStatus());
            } else {
                return new Response(ErrorCode.STATUS_FAILURE.getStatus(), "Not found data");
            }
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_FAILURE.getStatus(), e.getMessage());
        }
    }

}






