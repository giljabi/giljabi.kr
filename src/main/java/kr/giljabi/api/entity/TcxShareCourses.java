package kr.giljabi.api.entity;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Getter
@Table(name = "tcxsharecourses")
public class TcxShareCourses implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileid")
    private Long fileId;

    @Column(name = "trtime", nullable = false, length = 14)
    private String trTime;

    @Column(name = "userip", nullable = false, length = 32)
    private String userIp;

    @Column(name = "userno", nullable = false, length = 128)
    private String userNo;

    @Column(name = "filehash", nullable = false, length = 32)
    private String fileHash;

    @Column(name = "pcfilename", nullable = false, length = 128)
    private String pcFileName;

    @Column(name = "pathname", nullable = false, length = 128)
    private String pathName;

    @Column(name = "readcnt", nullable = false)
    private Integer readCnt;

    @Column(name = "downcnt", nullable = false)
    private Integer downCnt;

    @Column(name = "recommend", nullable = false)
    private Integer recommend;

    @Column(name = "wptcnt", nullable = false)
    private Integer wptCnt;

    @Column(name = "trkcnt", nullable = false)
    private Integer trkCnt;

    @Column(name = "distance", nullable = false)
    private Integer distance;

    @Column(name = "elevation", nullable = false)
    private Integer elevation;

    @Column(name = "slope", nullable = false, precision = 4, scale = 2)
    private Double slope;

    @Column(name = "traveltime", nullable = false)
    private Integer travelTime;

    @Column(name = "memo", nullable = false, columnDefinition = "TEXT")
    private String memo;

    @Column(name = "flag", length = 1)
    private String flag;
}
