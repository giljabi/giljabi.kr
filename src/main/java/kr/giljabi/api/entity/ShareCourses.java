package kr.giljabi.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "tcxsharecourses")
public class ShareCourses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileid")
    private Long fileId;

    @Column(name = "trtime", nullable = false)
    private String trTime;

    @Column(name = "userip", nullable = false)
    private String userIp;

    @Column(name = "userno", nullable = false)
    private String userNo;

    //pk
    @Column(name = "filehash", nullable = false)
    private String fileHash;

    @Column(name = "pcfilename", nullable = false)
    private String pcFileName;

    @Column(name = "pathname", nullable = false)
    private String pathName;

    @Column(name = "readcnt", nullable = false)
    private int readCnt;

    @Column(name = "downcnt", nullable = false)
    private int downCnt;

    @Column(name = "recommend", nullable = false)
    private int recommend;

    @Column(name = "wptcnt", nullable = false)
    private int wptCnt;

    @Column(name = "trkcnt", nullable = false)
    private int trkCnt;

    @Column(name = "distance", nullable = false)
    private int distance;

    @Column(name = "elevation", nullable = false)
    private int elevation;

    @Column(name = "slope", nullable = false)
    private Double slope;

    @Column(name = "traveltime", nullable = false)
    private int travelTime;

    @Column(name = "memo", nullable = false, columnDefinition = "TEXT")
    private String memo;

    @Column(name = "flag")
    private String flag;

    // Getters and Setters
}
