package kr.giljabi.api.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.28
 * @Description
 * gps 정보가 없으면 저장하지 않음
 */
@Entity
@Table(name = "gpsdata")
public class GiljabiGpsdataimg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gpsdata_id", nullable = false)
    private GiljabiGpsdata gpsdata;

    @Column(nullable = false)
    private LocalDateTime datetime;

    @Column(nullable = false, length = 255)
    private String filename; //uuid

    @Column(nullable = false, length = 10)
    private String fileext;

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int length;

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

    @Column(length = 16)
    private String exif;    //exif 버전
}
