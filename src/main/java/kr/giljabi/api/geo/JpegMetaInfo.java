package kr.giljabi.api.geo;

import com.drew.lang.GeoLocation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JpegMetaInfo {
    private int imageWidth;
    private int imageLength;
    private String make;
    private String model;
    private int orientation;
    private String dateTime;
    private String exifVersion;
    private GeoLocation geoLocation;
    private double altitude;
}