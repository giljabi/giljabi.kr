package kr.giljabi.api.utils;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.geo.gpx.TrackPoint;
import kr.giljabi.api.request.RequestGpsDataDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

public class CommonUtils {
    public static String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

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

    /**
     * session정보가 있거나 없거나 반드시 리턴해야 함
     * @param request
     * @return
     */
    public static UserInfo getSessionByUserinfo(HttpServletRequest request) {
        JwtProvider jwtProvider = new JwtProvider();
        UserInfo userInfo = new UserInfo();
        try {
            HttpSession session = request.getSession();
            Jws<Claims> claims = jwtProvider.getClaims((String) session.getAttribute("token"));
            userInfo = new Gson().fromJson((String) claims.getBody().get("userinfo"), UserInfo.class);
        } catch(Exception e) {
            userInfo.setLevel("00");
            userInfo.setUserid("sonnim@giljabi.kr");
            userInfo.setUsername("손님");
        }
        return userInfo;
    }

    public static GiljabiGpsdata makeGiljabiGpsdata(String userAddress, String apiName,
                                              RequestGpsDataDTO gpsDataDTO,
                                              long decompressedSize, long compressedSize,
                                              String fileurl, String userid) {
        GiljabiGpsdata gpsdata = new GiljabiGpsdata();
        gpsdata.setDistance(gpsDataDTO.getDistance());
        gpsdata.setFileext(gpsDataDTO.getFileext());
        gpsdata.setFileurl(fileurl);
        gpsdata.setSpeed(gpsDataDTO.getSpeed());
        gpsdata.setTrackname(gpsDataDTO.getTrackName());
        gpsdata.setTrkpt(gpsDataDTO.getTrkpt());
        gpsdata.setUserid(userid);
        gpsdata.setUuid(gpsDataDTO.getUuid()); //filename
        gpsdata.setWpt(gpsDataDTO.getWpt());
        gpsdata.setFilesize(decompressedSize);
        gpsdata.setFilesizecompress(compressedSize);
        gpsdata.setApiname(apiName);
        gpsdata.setUserip(userAddress);

        return gpsdata;
    }


    public static String makeGpsdataObjectName(String bucketName, RequestGpsDataDTO gpsDataDTO) {
        String filename = String.format("%s/%s/%s.%s",
                bucketName,
                CommonUtils.getFileLocation(gpsDataDTO.getUuid()),
                gpsDataDTO.getUuid(),
                gpsDataDTO.getFileext());
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
}
