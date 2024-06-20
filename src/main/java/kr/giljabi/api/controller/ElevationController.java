package kr.giljabi.api.controller;

import com.github.diogoduailibe.lzstring4j.LZString;
import io.swagger.annotations.ApiOperation;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.entity.GpsElevation;
import kr.giljabi.api.entity.GpxRecommend;
import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.geo.*;
import kr.giljabi.api.geo.gpx.TrackPoint;
import kr.giljabi.api.request.RequestElevationSaveData;
import kr.giljabi.api.response.GiljabiResponse;
import kr.giljabi.api.response.Gpx100Response;
import kr.giljabi.api.response.Mountain100;
import kr.giljabi.api.service.GiljabiGpsDataService;
import kr.giljabi.api.service.GiljabiGpxRecommendService;
import kr.giljabi.api.service.GoogleService;
import kr.giljabi.api.request.RequestElevationData;
import kr.giljabi.api.response.Response;
import kr.giljabi.api.service.MinioService;
import kr.giljabi.api.utils.CommonUtils;
import kr.giljabi.api.utils.ErrorCode;
import kr.giljabi.api.utils.MyHttpUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gpx track 정보를 tcx 변환
 *
 * @author eahn.park@gmail.com
 * 2018.10.26 gpx to tcx Project start....
 * 2021.09.17 Spring boot 2.5.4
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class ElevationController {
    private final GiljabiGpsDataService gpsService;
    private final GiljabiGpxRecommendService gpxRecommendService;

    private final GoogleService googleService;

    private final MinioService minioService;
    private UserInfo userInfo;

    @Value("${giljabi.mountain100.path}")
    private String mountain100Path;

//    @Value("${giljabi.google.elevation.path}")
//    private String elevationPath;

    @Value("${giljabi.gpx.path}")
    private String gpxPath;

    @Value("${minio.bucketPublic}")
    private String bucketPublic;

    @Value("${minio.bucketPrivate}")
    private String bucketPrivate;

    @PostMapping("/api/1.0/elevation")
    @ApiOperation(value = "고도정보", notes = "google elevation api 이용하여 고도정보를 받아오는 api")
    public Response getElevation(final HttpServletRequest request,
            final @Valid @RequestBody RequestElevationData requestElevationData) {
        ArrayList<TrackPoint> list;
        Response response;
        try {
            if (requestElevationData.getTrackPoint().size() == 0)
                new Exception("입력된 트랙정보가 없습니다.");

            //tcp socket exception 방지, 개발초기에 간혹 발생했었는데 이제는 이런 문제는 없는듯...
            //googleService.checkGoogle();
            list = googleService.getElevation(request, requestElevationData);

            return new Response(list);
//            return getMountainData();
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    /**
     * /api/1.0/mountain100 for mountain100Select combobox
     * @return
     */
    @GetMapping("/api/1.0/mountain100")
    @ApiOperation(value = "산림청 100대 명산",
            notes = "<h3>한국등산트레킹지원센터_산림청 100대명산</h3><br>" +
                    "산림청 100대명산의 POI(관심지점), 갈림길(방면), 노면정보를 제공하는 GPX 포맷의 공간정보 파일데이터<br>" +
                    "https://www.data.go.kr/data/15098177/fileData.do?recommendDataYn=Y<br>")
    public Response getMountainList100() {
        try {
            List<Mountain100> list = gpxRecommendService.findTrackNamesByGpxGroup("forest100");
            return new Response(list);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    /**
     * editor getMountainGpxLists
     * gxpgroup: forest100
     * minio 서비스로 변경
     * @param filename + "-*\\.gpx$": abc-*.gpx
     * @return
     */
    @GetMapping("/api/1.0/mountainGpxLists/{gxpgroup}/{filename}")
    @ApiOperation(value = "산림청 100대 명산 이름으로 검색한 gpx 파일 목록은 2개 이상일 수 있어 " +
            "목록을 반환하고 클라이언트에서 파일을 순차적으로 요청한다.")
    public Response getMountainList100Files(@PathVariable String gxpgroup,
                                            @PathVariable String filename) {
        try {
            List<String> list = gpxRecommendService
                    .findByGpxgroupAndTracknameOrderByFilename(gxpgroup, filename);
            return new Response(list);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    /**
     *
     * @param directory: forest100
     * @param filename
     * @return
     */
    @GetMapping("/api/1.0/mountainGpx/{directory}/{filename}")
    @ApiOperation(value = "산림청 100대 명산 gpx 경로정보, gpx 파일로 관리하고 압축해서 전송한다 ")
    public Response getMountainList100Gpxfile(@PathVariable String directory,
                                              @PathVariable String filename) {
        try {
            Gpx100Response gpx100Response = new Gpx100Response();
            String xmlDataLink = minioService.readFileContentByString(bucketPublic,
                    directory + "/" + filename);
            gpx100Response.setTrackName(filename);
            gpx100Response.setXmlData(xmlDataLink);
            return new Response(Optional.of(gpx100Response));
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    @PostMapping("/api/1.0/saveElevation")
    @ApiOperation(value = "GPX정보", notes = "GPX정보를 저장한다.")
    public Response saveElevation(final HttpServletRequest request,
                                  final @Valid @RequestBody RequestElevationSaveData elevationSaveData) {
        try {
            userInfo = CommonUtils.getSessionByUserinfo(request);
            String xmlData = LZString.decompressFromUTF16(elevationSaveData.getXmlData());

            String uuid = CommonUtils.generateUUID().toString();
            String filename = CommonUtils.makeGpsdataObjectName(
                    gpxPath,
                    uuid,
                    elevationSaveData.getFileExt());

            String compressedXml = LZString.compressToUTF16(xmlData);
            InputStream inputStream = new ByteArrayInputStream(compressedXml.getBytes(StandardCharsets.UTF_8));
            String savedFilename = minioService.putObject(bucketPrivate,
                    filename, inputStream, CommonUtils.BINARY_CONTENT_TYPE);

            //DB에 저장 필요
            GiljabiGpsdata gpsdata = new GiljabiGpsdata();
            gpsdata.setTrackname(elevationSaveData.getGpxName());
            gpsdata.setFileext(elevationSaveData.getFileExt());
            gpsdata.setWpt(elevationSaveData.getWayPointCount());
            gpsdata.setTrkpt(elevationSaveData.getTrackPointCount());
            gpsdata.setDistance(elevationSaveData.getDistance());
            gpsdata.setFilesize(xmlData.length());
            gpsdata.setFilesizecompress(elevationSaveData.getXmlData().length());
            gpsdata.setFileurl(savedFilename);
            gpsdata.setSpeed(elevationSaveData.getSpeed());
            gpsdata.setUserid(userInfo.getUserid());
            gpsdata.setUuid(uuid); //filename
            gpsdata.setApiname(elevationSaveData.getApiName());
            gpsdata.setUserip(MyHttpUtils.getClientIp(request));
            log.info("saveElevation: " + savedFilename);
            gpsService.saveGpsdata(gpsdata);

            GiljabiResponse giljabiResponse = new GiljabiResponse();
            giljabiResponse.setFileKey(gpsdata.getUuid());
            giljabiResponse.setFilePath(savedFilename);
            return new Response(giljabiResponse);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    private Response getMountainData() {
        Response response = new Response(0, "정상 처리 되었습니다.");
        ArrayList<Geometry3DPoint> data = new ArrayList<>();
        data.add(new Geometry3DPoint(127.013472, 37.953889, 153.4));
        data.add(new Geometry3DPoint(127.045492, 37.921015, 107.4));
        data.add(new Geometry3DPoint(127.114628, 37.912896, 325.3));
        data.add(new Geometry3DPoint(127.162436, 37.974824, 205.6));
        data.add(new Geometry3DPoint(127.139509, 37.984367, 265.8));
        response.setData(data);
        return response;
    }
}
