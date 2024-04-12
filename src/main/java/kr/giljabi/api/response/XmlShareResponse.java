package kr.giljabi.api.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Data
public class XmlShareResponse {
    private String fileId;
    private String trackName;
    private String xmlData;
    private String fileType;
}
