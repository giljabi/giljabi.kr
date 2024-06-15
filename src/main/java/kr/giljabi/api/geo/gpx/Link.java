package kr.giljabi.api.geo.gpx;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Link {

    @XmlAttribute(name = "href")
    private String href;

}

