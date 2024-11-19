package kr.giljabi.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * @Author : eahn.park@gmail.com
 * @Date : 2024.05.28
 * @Description
 * gps 정보가 없으면 저장하지 않음
 */
@Setter
@Getter
@Entity
@Table(name = "gpsdataimage")
public class GiljabiGpsdataImage implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gpsdata_id", nullable = false)
    @JsonIgnore
    private GiljabiGpsdata gpsdata;

    //@Column(nullable = false, length = 36)
    @Column(nullable = false)
    private Timestamp createat = Timestamp.from(Instant.now());

    //@Column(nullable = false, length = 36)
    @Column(nullable = false)
    private Timestamp changeat = Timestamp.from(Instant.now());
    
    @Column(nullable = true)
    private String originaldatetime;

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(nullable = false)
    private double ele;

    @Column(length = 255)
    private String make; //카메라 제조사

    @Column(length = 255)
    private String model;//카메라 모델

    @Column(nullable = false, length = 255)
    private String fileurl; //uuid

    @Column(nullable = false, length = 3)
    private String fileext;

    @Column(nullable = false)
    private long filesize;

    @Column(nullable = false, length = 255)
    private String originalfname;

    @Column(length = 36)
    private String userip;
}

