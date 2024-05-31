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
    private ArrayList<GiljabiResponseGpsdataImageDTO> gpsdataimages;
}
