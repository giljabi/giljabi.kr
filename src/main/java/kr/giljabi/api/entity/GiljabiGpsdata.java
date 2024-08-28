package kr.giljabi.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * @Author : eahn.park@gmail.com
 * @Date : 2024.05.28
 * @Description
 */
@Entity
@Table(name = "gpsdata")
@Setter
@Getter
public class GiljabiGpsdata implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 36, unique = true)
    private String uuid;

    //@Column(nullable = false, length = 36)
    @Column(nullable = false)
    private Timestamp createat = Timestamp.from(Instant.now());

    //@Column(nullable = false, length = 36)
    @Column(nullable = false)
    private Timestamp changeat = Timestamp.from(Instant.now());

    @Column(nullable = false, length = 255)
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @NotNull(message = "이메일은 필수 입력 항목입니다")
    private String userid;

    @Column(nullable = false)
    private int wpt;

    @Column(nullable = false)
    private long trkpt;

    @Column(nullable = false, length = 255)
    private String trackname;

    @Column(nullable = false)
    private double speed;

    @Column(nullable = false)
    private double distance;

    @Column(nullable = true, length = 512)
    private String fileurl;

    //gpx, tcx
    @Column(nullable = true, length = 16)
    private String fileext;

    @Column(nullable = true)
    private long filesize;

    @Column(nullable = true, length = 16)
    private String apiname;

    @Column(nullable = true)
    private long filesizecompress;

    @Column(nullable = false)
    private boolean shareflag = false;

    @Column(nullable = true)
    private String userip;

    @OneToMany(mappedBy = "gpsdata", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GiljabiGpsdataImage> gpsdataimages;
    public void addGpsImage(GiljabiGpsdataImage gpsImage) {
        gpsdataimages.add(gpsImage);
        gpsImage.setGpsdata(this);
    }

}
