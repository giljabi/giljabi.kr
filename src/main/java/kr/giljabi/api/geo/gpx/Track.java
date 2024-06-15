package kr.giljabi.api.geo.gpx;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Track {

    @XmlElement(name = "trkseg")
    private TrackSegment trkseg;
}
