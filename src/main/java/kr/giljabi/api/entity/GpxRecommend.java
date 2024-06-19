package kr.giljabi.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "gpxrecommend")
public class GpxRecommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trackname", length = 128)
    private String trackname;
    @Column(name = "trackkorean", length = 128)
    private String trackkorean;

    @Column(name = "gpxgroup", length = 16)
    private String gpxgroup;

    @Column(name = "filename", length = 64, nullable = false, unique = true)
    private String filename;

    //@Column(nullable = false, length = 36)
    @Column(nullable = false)
    private Timestamp createat = Timestamp.from(Instant.now());

    //@Column(nullable = false, length = 36)
    @Column(nullable = false)
    private Timestamp changeat = Timestamp.from(Instant.now());

    @Column(name = "readcnt")
    private Integer readcnt;
}
