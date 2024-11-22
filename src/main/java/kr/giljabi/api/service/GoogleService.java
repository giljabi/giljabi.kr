package kr.giljabi.api.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.entity.GpsElevation;
import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.geo.GoogleElevationData;
import kr.giljabi.api.geo.gpx.*;
import kr.giljabi.api.request.RequestElevationData;
import kr.giljabi.api.request.RequestGpsDataDTO;
import kr.giljabi.api.utils.CommonUtils;
import kr.giljabi.api.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.URL;
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
    private final JwtProviderService jwtProviderService;
    //private final MinioService minioService;

    @Value("${giljabi.google.elevation.apikey}")
    private String googleApikey;

    @Value("${giljabi.google.elevation.elevationUrl}")
    private String elevationUrl;

    @Value("${giljabi.google.elevation.googleGetCount}")
    private int googleGetCount;

    @Value("${giljabi.gpx.path}")
    private String gpxPath;



    //장시간 호출이 없는 경우 socket error가 발생하므로 미리 호출한다
    //key를 사용해도 되지만 google api 호출건수가 증가하므로 key를 사용하지 않게 한다.
    public void checkGoogle() throws Exception {
        String paramter = "37.566102885810565,126.97594723621106";
        String jsonElevation = requestElevationService(paramter, "");
        log.info(jsonElevation);
    }

    public ArrayList<TrackPoint> getElevation(HttpServletRequest request,
                                                   RequestElevationData requestElevationData) throws Exception {
        UserInfo userInfo = jwtProviderService.getSessionByUserinfo(request);

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

        //구글에 요청한 횟수, 경과시간을 관리한다.
        GpsElevation gpsElevation = insertElevation(request, userInfo, trackPoint, maxPage, startTime, endTime);

        //gpsdata insert, 압축안된 경로파일은 DB, 파일 저장안함, 구분자 고민중
/*        GiljabiGpsdata gpsdata = saveGpxdata(request, userInfo, trackPoint, returnPoint, gpsElevation);
        gpsService.save(gpsdata);*/
        return returnPoint;
    }

        private GpsElevation insertElevation(HttpServletRequest request, UserInfo userInfo,
                                         List<RequestElevationData.Geometry2DPoint> trackPoint,
                                         int maxPage, long startTime, long endTime) {
        GpsElevation gpsElevation = new GpsElevation();
        gpsElevation.setApiname("getElevation");
        gpsElevation.setUuid(UUID.randomUUID().toString());

        String logicalFileName = CommonUtils.makeGpsdataObjectPath(gpsElevation.getUuid());

        //String fileurl = String.format("%s/%s", gpxPath, logicalFileName);
        gpsElevation.setFileurl(logicalFileName);
        gpsElevation.setTranstime(endTime - startTime);
        gpsElevation.setTrkpt(trackPoint.size());
        gpsElevation.setUserid(userInfo.getUserid());
        gpsElevation.setUserip(request.getRemoteAddr());
        gpsElevation.setWpt(0);
        gpsElevation.setReqcnt(maxPage);
        giljabiGpsElevationService.saveElevation(gpsElevation);
        return gpsElevation;
    }

    /**
     * 구글에서 받은 elevation정보를 이용해 xml을 만들고 압축해서 저장한다.
     * 서버에서 압축하지 않고, 클라이언트로 전송 후 클라이언트에서 압축해서 전송하는 것으로 변경 해야 함
     * 절대로 서버에서 compress/decompress 해서는 안됨....
     *
     * 2024.11.22
     * 압축안된 파일은 파일저장 중지, DB 저장도 중지, gpsdata에 압축 유무 추가 고민중
     * @param request
     * @param userInfo
     * @param trackPoint
     * @param returnPoint
     * @param gpsElevation
     * @return
     * @throws Exception
     */
    private GiljabiGpsdata saveGpxdata(HttpServletRequest request,
                                       UserInfo userInfo,
                                       List<RequestElevationData.Geometry2DPoint> trackPoint,
                                       ArrayList<TrackPoint> returnPoint,
                                       GpsElevation gpsElevation) throws Exception {
        Gpx gpx = makeGpxXml(returnPoint);
        //String compressedXml = LZString.compressToUTF16(gpxXml);
        String compressedXml = gpx.getXml();    //서버에서 압축 절대금지..1건으로 서버 다운될 수 있음
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

        String physicalPath = CommonUtils.makeGpsdataObjectPath(gpsDataDTO.getUuid());
        String savedFilename = FileUtils.saveFile(gpxPath + physicalPath,
                gpsDataDTO.getUuid(), compressedXml);

        GiljabiGpsdata gpsdata = CommonUtils.makeGiljabiGpsdata(request.getRemoteAddr(),
                "makeElevation",
                gpsDataDTO,
                compressedXml.getBytes().length,    //decompressed
                compressedXml.getBytes().length,
                savedFilename,
                userInfo.getUserid(),
                "" /* 사용자 구분은 나중에...*/
        );
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



