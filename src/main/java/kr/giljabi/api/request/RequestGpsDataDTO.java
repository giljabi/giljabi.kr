package kr.giljabi.api.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RequestGpsDataDTO {
    private double speed;
    private String xmldata;
    private String fileext;
    private String filename;
    private String pathname;
    private int wpt;
    private long trkpt;
    private double distance;

}

