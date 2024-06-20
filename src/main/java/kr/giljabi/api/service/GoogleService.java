package kr.giljabi.api.service;

import com.github.diogoduailibe.lzstring4j.LZString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.entity.GpsElevation;
import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.geo.Geometry3DPoint;
import kr.giljabi.api.geo.GoogleElevationData;
import kr.giljabi.api.geo.gpx.*;
import kr.giljabi.api.request.RequestElevationData;
import kr.giljabi.api.request.RequestGpsDataDTO;
import kr.giljabi.api.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Google elevation 서비스 
 * eahn.park@gmail.com
 * 2021.10.01
 * elevation api 이후 direction api를 사용해서 openrouteservice를 바꿀 수 있을지 확인...
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {
    private final GiljabiGpsElevationService giljabiGpsElevationService;
    private final GiljabiGpsDataService gpsService;
    private final MinioService minioService;

    @Value("${giljabi.google.elevation.apikey}")
    private String googleApikey;

    @Value("${giljabi.google.elevation.elevationUrl}")
    private String elevationUrl;

    @Value("${giljabi.google.elevation.googleGetCount}")
    private int googleGetCount;

    @Value("${giljabi.gpx.path}")
    private String gpxPath;

    @Value("${minio.serviceUrl}")
    private String s3url;

    @Value("${minio.bucketPrivate}")
    private String bucketPrivate;

    //장시간 호출이 없는 경우 socket error가 발생하므로 미리 호출한다
    //key를 사용해도 되지만 google api 호출건수가 증가하므로 key를 사용하지 않게 한다.
    public void checkGoogle() throws Exception {
        String paramter = "37.566102885810565,126.97594723621106";
        String jsonElevation = requestElevationService(paramter, "");
        log.info(jsonElevation);
    }

    public ArrayList<TrackPoint> getElevation(HttpServletRequest request,
                                                   RequestElevationData requestElevationData) throws Exception {
        UserInfo userInfo = CommonUtils.getSessionByUserinfo(request);

        List<RequestElevationData.Geometry2DPoint> trackPoint = requestElevationData.getTrackPoint();
        ArrayList<TrackPoint> returnPoint = new ArrayList<>();

        //elevation api는 하루 2500요청
        //get을 사용해하므로 request url의 길이는 8192를 넘지 않아야 한다.
        int maxPage;
        if(trackPoint.size() % googleGetCount == 0)
            maxPage = (trackPoint.size() / googleGetCount);
        else
            maxPage = (trackPoint.size() / googleGetCount) + 1;

        long startTime = System.currentTimeMillis();

        try {
            int index = 0;
            StringBuilder buffer = new StringBuilder();
            Gson gson = new GsonBuilder().create();

            log.info("maxPage:" + maxPage);

            TrackPoint previousPoint = null;
            for (int j = 1; j <= maxPage; j++) {
                for (; index < googleGetCount * j; index++) {
                    if (index == trackPoint.size())
                        break;
                    buffer.append(String.format("%s,%s|"
                            , trackPoint.get(index).getLat(), trackPoint.get(index).getLng()));
                }
                String jsonElevation = requestElevationService(buffer.substring(0, buffer.length() - 1), googleApikey);
                GoogleElevationData googleElevation = gson.fromJson(jsonElevation, GoogleElevationData.class);
                List<GoogleElevationData.Results> results = googleElevation.getResults();

                DecimalFormat decimalFormat = new DecimalFormat("#.#");
                for(GoogleElevationData.Results googleLocation : results) {
                    TrackPoint point = new TrackPoint(
                            googleLocation.getLocation().getLng(),
                            googleLocation.getLocation().getLat(),
                            Double.parseDouble(decimalFormat.format(googleLocation.getElevation()))
                    );
                    if(previousPoint != null) {
                        double distance = CommonUtils.getDistance(previousPoint, point);
                        point.setDist(previousPoint.getDist() + distance);
                    }
                    returnPoint.add(point);
                    previousPoint = point;
                }
                buffer.setLength(0);
                //너무 짧은 간격으로 호출하면 문제가 있을 수 있다...1초 지연
                TimeUnit.SECONDS.sleep(1);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
        long endTime = System.currentTimeMillis();

        GpsElevation gpsElevation = insertElevation(request, userInfo, trackPoint, maxPage, startTime, endTime);

//gpsdata insert
        GiljabiGpsdata gpsdata = saveGpxdata(request, userInfo, trackPoint, returnPoint, gpsElevation);
        gpsService.saveGpsdata(gpsdata);
        return returnPoint;
    }

    private GpsElevation insertElevation(HttpServletRequest request, UserInfo userInfo, List<RequestElevationData.Geometry2DPoint> trackPoint, int maxPage, long startTime, long endTime) {
        GpsElevation gpsElevation = new GpsElevation();
        gpsElevation.setApiname("getElevation");
        gpsElevation.setUuid(UUID.randomUUID().toString());

        String objectName = CommonUtils.makeGpsdataObjectName(gpxPath,
                gpsElevation.getUuid(),
                "gpx");

        String fileurl = String.format("%s/%s/%s",
                s3url, bucketPrivate, objectName);
        gpsElevation.setFileurl(fileurl);
        gpsElevation.setTranstime(endTime - startTime);
        gpsElevation.setTrkpt(trackPoint.size());
        gpsElevation.setUserid(userInfo.getUserid());
        gpsElevation.setUserip(request.getRemoteAddr());
        gpsElevation.setWpt(0);
        gpsElevation.setReqcnt(maxPage);
        giljabiGpsElevationService.saveElevation(gpsElevation);
        return gpsElevation;
    }

    private GiljabiGpsdata saveGpxdata(HttpServletRequest request, UserInfo userInfo, List<RequestElevationData.Geometry2DPoint> trackPoint, ArrayList<TrackPoint> returnPoint, GpsElevation gpsElevation) throws Exception {
        Gpx gpx = makeGpxXml(returnPoint);
        String gpxXml = gpx.getXml();
        String compressedXml = LZString.compressToUTF16(gpxXml);
        List<TrackPoint> trkpt = gpx.getTrk().getTrkseg().getTrkpt();
        TrackPoint lastTrackPoint = trkpt.get(trkpt.size() - 1);

        RequestGpsDataDTO gpsDataDTO = new RequestGpsDataDTO();
        gpsDataDTO.setUuid(gpsElevation.getUuid());
        gpsDataDTO.setTrackName(""); //없음
        gpsDataDTO.setWpt(0);
        gpsDataDTO.setTrkpt(trackPoint.size());
        gpsDataDTO.setDistance((long)lastTrackPoint.getDist());
        gpsDataDTO.setSpeed(2);
        gpsDataDTO.setXmldata("");
        gpsDataDTO.setFileext("gpx");
        gpsDataDTO.setFilename("");

        String objectName = CommonUtils.makeGpsdataObjectName(gpxPath,
                gpsDataDTO.getUuid(),
                gpsDataDTO.getFileext());

        InputStream inputStream = new ByteArrayInputStream(compressedXml.getBytes(StandardCharsets.UTF_8));
        String savedFilename = minioService.putObject(bucketPrivate,
                objectName, inputStream, CommonUtils.BINARY_CONTENT_TYPE);

        GiljabiGpsdata gpsdata = CommonUtils.makeGiljabiGpsdata(request.getRemoteAddr(),
                "makeElevation",
                gpsDataDTO,
                gpxXml.getBytes().length,    //decompressed
                compressedXml.getBytes().length,
                s3url + "/" + savedFilename,
                userInfo.getUserid());
        log.info("getElevation: " + savedFilename);
        return gpsdata;
    }

    private Gpx makeGpxXml(ArrayList<TrackPoint> returnPoint) throws JAXBException {
        Gpx gpx = new Gpx();
        gpx.setCreator("giljabi");
        gpx.setVersion("1.1");

        Metadata metadata = new Metadata();
        metadata.setName("giljabi-gpx");
        Link link = new Link();
        link.setHref("https://giljabi.kr");
        metadata.setLink(link);
        metadata.setDesc("giljabi");
        metadata.setCopyright("giljabi.kr");
        metadata.setSpeed(2);
        metadata.setTime(LocalDateTime.now());
        gpx.setMetadata(metadata);

        //Segment는 여러개 있을 수 있으므로 여러개의 Segment를 가지는 Track을 생성
        TrackSegment trackSegment = new TrackSegment();
        trackSegment.setTrkpt(returnPoint);
        Track track = new Track();
        track.setTrkseg(trackSegment);

        gpx.setTrk(track);
//
//        JAXBContext context = JAXBContext.newInstance(Gpx.class);
//        Marshaller marshaller = context.createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//        StringWriter writer = new StringWriter();
//        marshaller.marshal(gpx, writer);
//        String gpxXml = writer.toString();
//        log.info(gpxXml);
        return gpx;
    }

    private String requestElevationService(String parameter, String key) throws Exception {
        String requestUrl = String.format("%s?locations=%s&key=%s", elevationUrl, parameter, key);
        URL elevationUrl = new URL(requestUrl);
        HttpsURLConnection httpConnection = (HttpsURLConnection)elevationUrl.openConnection();
        httpConnection.setRequestMethod("GET");

        StringBuilder stringBuilder = new StringBuilder();
        log.info("http status:" + httpConnection.getResponseCode());
        if(httpConnection.getResponseCode() == HttpStatus.SC_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            String inputLine;
            while((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            if(in != null) in.close();
        }
        return stringBuilder.toString();
    }

}
