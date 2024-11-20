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
    private String trackName;
    private int wpt;
    private long trkpt;
    private double distance;
    private String uuid;        //파일명을 구분하기 위한 UUID
    private String userUUID;    //사용자 구분을 위한 UUID
    //private String fileid;      //파일ID가 있으면 공유기능으로 보는 것이므로 추가저장(db, file)
}


