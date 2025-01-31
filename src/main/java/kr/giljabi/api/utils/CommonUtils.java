package kr.giljabi.api.utils;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.geo.JpegMetaInfo;
import kr.giljabi.api.geo.gpx.TrackPoint;
import kr.giljabi.api.jwt.JwtProvider;
import kr.giljabi.api.request.RequestGpsDataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class CommonUtils {
    public static String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public final static String BINARY_CONTENT_TYPE = "application/octet-stream";
    public final static String TEXT_CONTENT_TYPE = "application/text";

    public static String GILJABI_UUID = "GILJABI_UUID";

    private static JwtProvider jwtProvider;

    @Autowired
    public CommonUtils(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    public static String getCurrentTime(String format) {
        if (format == null || format.isEmpty()) {
            format = DEFAULT_TIMESTAMP_FORMAT;
        }
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }

    public static Timestamp stringToTimeStamp(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT);
            Date parsedDate = dateFormat.parse(dateString);
            return new Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            System.out.println("Error in parsing date: " + e.getMessage());
            return null;
        }
    }
    public static String longTimeToTimeStamp(long timestamp, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(timestamp));
    }
    public static Timestamp getTimeStamp() {
        Date date = new Date();
        return new Timestamp(date.getTime());
    }

    public static UUID generateUUID() {
        return UUID.randomUUID();
    }

    public static String generateUUIDFilename(String fileExt) {
        return UUID.randomUUID() + fileExt;
    }

    public static String getFileLocation(String uuidKey) {
        return String.format("%s/%s", CommonUtils.getCurrentTime("YYYYMM"), uuidKey);
    }


    public static GiljabiGpsdata makeGiljabiGpsdata(String userAddress, String apiName,
                                              RequestGpsDataDTO gpsDataDTO,
                                              long decompressedSize, long compressedSize,
                                              String fileurl, String userid, String useruuid) {
        GiljabiGpsdata gpsdata = new GiljabiGpsdata();
        gpsdata.setDistance(gpsDataDTO.getDistance());
        gpsdata.setFileext(gpsDataDTO.getFileext());
        gpsdata.setFileurl(fileurl);
        gpsdata.setSpeed(gpsDataDTO.getSpeed());
        gpsdata.setTrackname(normalizeToNFC(gpsDataDTO.getTrackName()));
        gpsdata.setTrkpt(gpsDataDTO.getTrkpt());
        gpsdata.setUserid(userid);
        gpsdata.setUuid(gpsDataDTO.getUuid()); //filename
        gpsdata.setWpt(gpsDataDTO.getWpt());
        gpsdata.setFilesize(decompressedSize);
        gpsdata.setFilesizecompress(compressedSize);
        gpsdata.setApiname(apiName);
        gpsdata.setUserip(userAddress);
        gpsdata.setUseruuid(useruuid);

        return gpsdata;
    }

    /**
     * 맥에서 저장하는 경우는 NFC(완성형)로 정규화, NFD(조합형)으로 저장하면 풀어쓰기가 됨
     * @param input
     * @return
     */
    public static String normalizeToNFC(String input) {
        if (input == null) return null;
        return Normalizer.normalize(input, Normalizer.Form.NFC);
    }

    public static String makeGpsdataObjectName(String bucketName,
                                               String uuid,
                                               String fileext) {
        String filename = String.format("%s/%s/%s/%s.%s",
                bucketName,
                CommonUtils.getCurrentTime("YYYYMM"), ///yyyymm/uuid
                uuid,   //디렉토리 명
                uuid,   //파일 명
                fileext);
        return filename;
    }

    public static String makeGpsdataObjectPath(String uuid) {
        String filename = String.format("/%s/%s",
                CommonUtils.getCurrentTime("YYYYMM"), ///yyyymm/uuid
                uuid);  // 파일마다 디렉토리 생성
        return filename;
    }

    private static final double EARTH_RADIUS = 6371e3; // Earth radius in meters
    public static double getDistance(TrackPoint from, TrackPoint to) {
        double dLat = Math.toRadians(to.getLat() - from.getLat());
        double dLon = Math.toRadians(to.getLng() - from.getLng());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(from.getLat())) * Math.cos(Math.toRadians(to.getLat())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * jpeg 파일의 메타데이터를 추출한다.
     * @param metadata
     * @return
     * @throws Exception
     */
    public static JpegMetaInfo getMetaData(Metadata metadata) throws Exception {
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

    /*    public Response getMountainList100Files(@PathVariable String filename) {
        List<String> fileList = new ArrayList<>();
        try {
            //abc-*.gpx, gariwangsan*\\.gpx$
            fileList = minioService.listFiles(bucketData,
                    mountain100Path.substring(1) + "/",
                    filename + "*", "gpx");
            return new Response(fileList);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }*/
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> name.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}



