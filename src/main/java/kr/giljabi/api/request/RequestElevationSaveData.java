package kr.giljabi.api.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RequestElevationSaveData {
    private String gpxName;
    private String fileExt;
    private int wayPointCount;
    private int trackPointCount;
    private double distance;
    private String xmlData;

    public RequestElevationSaveData(String gpxName, String fileExt
            , int wayPointCount, int trackPointCount
            , double distance, String xmlData) {
        this.gpxName = gpxName;
        this.fileExt = fileExt;
        this.wayPointCount = wayPointCount;
        this.trackPointCount = trackPointCount;
        this.distance = distance;
        this.xmlData = xmlData;
    }

    @Override
    public String toString() {
        return "RequestElevationSaveData{" +
                "gpxName='" + gpxName + '\'' +
                ", fileExt='" + fileExt + '\'' +
                ", wayPointCount=" + wayPointCount +
                ", trackPointCount=" + trackPointCount +
                ", distance=" + distance +
                ", xmlData='" + xmlData + '\'' +
                '}';
    }
}
