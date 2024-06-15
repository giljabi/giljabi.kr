package kr.giljabi.api.geo.gpx;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.StringWriter;
import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "gpx")
@XmlAccessorType(XmlAccessType.FIELD)
public class Gpx {

    @XmlAttribute(name = "creator")
    private String creator;

    @XmlAttribute(name = "version")
    private String version;

    @XmlAttribute(name = "xsi:schemaLocation")
    private String schemaLocation = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/11.xsd";

    @XmlAttribute(namespace = "http://www.w3.org/2001/XMLSchema-instance")
    private String xsi = "http://www.w3.org/2001/XMLSchema-instance";

    @XmlAttribute(namespace = "http://www.topografix.com/GPX/1/1")
    private String xmlns = "http://www.topografix.com/GPX/1/1";

    @XmlAttribute(namespace = "http://www.garmin.com/xmlschemas/TrackPointExtension/v1")
    private String ns3 = "http://www.garmin.com/xmlschemas/TrackPointExtension/v1";

    @XmlAttribute(namespace = "http://www.garmin.com/xmlschemas/GpxExtensions/v3")
    private String ns2 = "http://www.garmin.com/xmlschemas/GpxExtensions/v3";

    @XmlElement(name = "metadata")
    private Metadata metadata;

    @XmlElement(name = "wpt")
    private List<Waypoint> wpt;

    @XmlElement(name = "trk")
    private Track trk;

    public String getXml() {
        try {
            JAXBContext context = JAXBContext.newInstance(Gpx.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(this, writer);
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
