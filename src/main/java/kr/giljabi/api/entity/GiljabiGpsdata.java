package kr.giljabi.api.entity;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.28
 * @Description
 */
@Entity
@Table(name = "gpsdata")
public class GiljabiGpsdata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 36)
    private String uuid;

    @Column(nullable = false)
    private LocalDateTime datetime;

    @Column(nullable = false, length = 255)
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @NotNull(message = "이메일은 필수 입력 항목입니다")
    private String email;
    @Column(nullable = false)
    private int wpt;

    @Column(nullable = false)
    private long trkpt;

    @Column(nullable = false, length = 255)
    private String gpxname;

    @Column(nullable = false)
    private float speed;

    @Column(nullable = false)
    private float distance;

    @Column(nullable = true, length = 255)
    private String filename;

    //gpx, tcx
    @Column(nullable = true, length = 16)
    private String fileext;

    @OneToMany(mappedBy = "gpsdata", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GiljabiGpsdataimg> gpsdataimgs;
}
