package kr.giljabi.api.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class GiljabiResponseGpsdataDTO {
    private long id;
    private String uuid;
    private String trackname;
    private String fileurl;
    private String fileext;
    private String xmldata;
    private ArrayList<GiljabiResponseGpsdataImageDTO> gpsdataimages;

    //2024.11.20, list view 추가
    private String apiname;
    private String createat;
    private int wpt;
    private long trkpt;
    private double speed;
    private double distance;
    private String useruuid;
    private String userid;
    private boolean shareflag;
}


