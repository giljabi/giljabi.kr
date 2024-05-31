package kr.giljabi.api.response;

import com.drew.lang.GeoLocation;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GiljabiResponseGpsdataImageDTO {
    private long id;
    private String originaldatetime;
    private double ele;
    private String fileurl;
    private String originalfname;

    //image 저장 후 응답시 사용됨
    private GeoLocation geoLocation;
    private double altitude;

}
