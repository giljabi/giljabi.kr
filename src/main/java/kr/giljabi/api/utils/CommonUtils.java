package kr.giljabi.api.utils;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import kr.giljabi.api.entity.UserInfo;

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


}
