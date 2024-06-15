package kr.giljabi.api.geo.gpx;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Metadata {
    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "link")
    private Link link;

    @XmlElement(name = "desc")
    private String desc;

    @XmlElement(name = "copyright")
    private String copyright;

    @XmlElement(name = "speed")
    private int speed;

    @XmlElement(name = "time")
    private LocalDateTime time;
}
