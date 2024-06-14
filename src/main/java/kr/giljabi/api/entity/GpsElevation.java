package kr.giljabi.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Setter
@Getter
@Table(name = "gpselevation")
public class GpsElevation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "apiname", length = 16)
    private String apiname;

    @Column(name = "uuid", length = 36, nullable = false, unique = true)
    private String uuid;

    @Column(nullable = false, length = 255)
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @NotNull(message = "이메일은 필수 입력 항목입니다")
    private String userid;

    @Column(name = "userip", length = 36, nullable = false)
    private String userip;

    @Column(name = "trackname", length = 128)
    private String trackname;

    @Column(nullable = false)
    private Timestamp createat = Timestamp.from(Instant.now());

    @Column(nullable = false)
    private Timestamp changeat = Timestamp.from(Instant.now());

    @Column(name = "transtime", nullable = false)
    private Integer transtime;

    @Column(name = "wpt", nullable = false)
    private Short wpt;

    @Column(name = "trkpt", nullable = false)
    private Short trkpt;

    @Column(name = "reqcnt")
    private Integer reqcnt;

    @PrePersist
    protected void onCreate() {
        createat = changeat = Timestamp.from(Instant.now());
    }

    @PreUpdate
    protected void onUpdate() {
        changeat = Timestamp.from(Instant.now());
    }
}