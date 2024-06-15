package kr.giljabi.api.geo.gpx;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Waypoint {

    @XmlAttribute(name = "lat")
    private double lat;

    @XmlAttribute(name = "lon")
    private double lon;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "sym")
    private String sym;
}
