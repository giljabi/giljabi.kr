package kr.giljabi.api.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.28
 * @Description
 */
@Entity
@Table(name = "gpsdata")
@Setter
@Getter
public class GiljabiGpsdata implements Serializable {
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 36)
    private String uuid;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createat;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime changeat;

    @Column(nullable = false, length = 255)
    //@Email(message = "이메일 형식이 올바르지 않습니다")
    //@NotNull(message = "이메일은 필수 입력 항목입니다")
    private String user;

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

    @OneToMany(mappedBy = "gpsdata", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GiljabiGpsdataImage> gpsdataimages;
    public void addGpsImage(GiljabiGpsdataImage gpsImage) {
        gpsdataimages.add(gpsImage);
        gpsImage.setGpsdata(this);
    }

}
