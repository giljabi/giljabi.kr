package kr.giljabi.api.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Data
public class Gpx100Response {
    private String trackName;
    private String xmlData;
}
