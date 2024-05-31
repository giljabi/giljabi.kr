package kr.giljabi.api.response;

import com.drew.lang.GeoLocation;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Data
public class GiljabiResponse {
    private String filePath;
    private String fileKey; //gps 파일이 메인키가 되고, gpx, tcx, jpg 파일이 서브키가 된다.
    private GeoLocation geoLocation;
    private double altitude;
    private String originalFileName;
}
