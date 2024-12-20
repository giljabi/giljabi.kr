package kr.giljabi.api.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RequestElevationSaveData {
    private String apiName;
    private String gpxName;
    private String fileExt;
    private int wayPointCount;
    private int trackPointCount;
    private double distance;
    private String xmlData;
    private double speed;
    private String userUUID;

    public RequestElevationSaveData(String apiName, String gpxName,
                                    String fileExt, int wayPointCount, int trackPointCount,
                                    double distance, String xmlData, double speed, String userUUID) {
        this.apiName = apiName;
        this.gpxName = gpxName;
        this.fileExt = fileExt;
        this.wayPointCount = wayPointCount;
        this.trackPointCount = trackPointCount;
        this.distance = distance;
        this.xmlData = xmlData;
        this.speed = speed;
        this.userUUID = userUUID;
    }

    @Override
    public String toString() {
        return "RequestElevationSaveData{" +
                "apiName='" + apiName + '\'' +
                ", gpxName='" + gpxName + '\'' +
                ", fileExt='" + fileExt + '\'' +
                ", wayPointCount=" + wayPointCount +
                ", trackPointCount=" + trackPointCount +
                ", distance=" + distance +
                ", xmlData='" + xmlData + '\'' +
                ", speed='" + speed + '\'' +
                ", userUUID='" + userUUID + '\'' +
                '}';
    }
}

