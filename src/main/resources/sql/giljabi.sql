
CREATE DATABASE giljabi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'giljabi'@'localhost' IDENTIFIED BY 'giljabi';

GRANT ALL PRIVILEGES ON giljabi.* TO 'giljabi'@'localhost';

CREATE TABLE tcxsharecourses (
                                   fileid int(8) NOT NULL AUTO_INCREMENT COMMENT '파일관리번호',
                                   trtime char(14) NOT NULL COMMENT '생성일시',
                                   userip varchar(32) NOT NULL COMMENT '사용자IP',
                                   userno varchar(128) NOT NULL COMMENT '사용자ID',
                                   filehash varchar(32) NOT NULL COMMENT '파일명해시값',
                                   pcfilename varchar(128) NOT NULL COMMENT '파일명',
                                   pathname varchar(128) NOT NULL COMMENT '경로명',
                                   readcnt int(8) NOT NULL COMMENT '조회수',
                                   downcnt int(8) NOT NULL COMMENT '경로명',
                                   recommend int(8) NOT NULL COMMENT '추천',
                                   wptcnt int(8) NOT NULL COMMENT '웨이포인트수',
                                   trkcnt int(8) NOT NULL COMMENT '트랙수',
                                   distance int(8) NOT NULL COMMENT '이동거리',
                                   elevation int(8) NOT NULL COMMENT '상승고도',
                                   slope decimal(4,2) NOT NULL COMMENT '평균경사도',
                                   traveltime int(8) NOT NULL COMMENT '시간',
                                   memo text NOT NULL COMMENT 'MEMO',
                                   flag char(1) DEFAULT NULL,
                                   PRIMARY KEY (fileid), UNIQUE(filehash)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='경로공유정보';

drop table gpsdata;
CREATE TABLE gpsdata (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         uuid VARCHAR(36) NOT null unique,
                         user VARCHAR(36) NOT null,
                         createat DATETIME NOT null DEFAULT now(),
                         changeat DATETIME NOT null DEFAULT now(),
                         wpt INT NOT NULL,
                         trkpt BIGINT NOT NULL,
                         trackname VARCHAR(255) NOT NULL,
                         speed double NOT NULL,
                         distance double NOT NULL,
                         fileurl varchar(255),
                         fileext varchar(3)
);

drop table gpsdataimage;
CREATE TABLE gpsdataimage (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              gpsdata_id INT NOT NULL,
                              createat DATETIME NOT NULL DEFAULT now(),
                              changeat DATETIME NOT NULL DEFAULT now(),
                              width INT NOT NULL,
                              height INT NOT NULL,
                              lat DOUBLE NOT NULL,
                              lng DOUBLE NOT NULL,
                              ele DOUBLE NOT NULL,
                              make VARCHAR(255),
                              model VARCHAR(255),
                              originaldatetime VARCHAR(36),
                              fileurl VARCHAR(255) NOT NULL,
                              fileext VARCHAR(8) NOT NULL,
                              FOREIGN KEY (gpsdata_id) REFERENCES gpsdata(id) ON DELETE CASCADE
);
