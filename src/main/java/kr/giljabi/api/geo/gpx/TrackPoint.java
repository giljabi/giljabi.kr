package kr.giljabi.api.geo.gpx;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class TrackPoint {
    public TrackPoint(double lng, double lat, double ele) {
        this.lat = lat;
        this.lng = lng;
        this.ele = ele;
    }

    @XmlAttribute(name = "lat")
    private double lat;

    @XmlAttribute(name = "lon")
    private double lng;

    @XmlElement(name = "ele")
    private double ele;

    @XmlElement(name = "ele")
    private LocalDateTime time;

    @XmlElement(name = "dist")
    private double dist;
}
