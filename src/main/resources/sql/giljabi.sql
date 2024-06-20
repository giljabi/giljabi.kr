-- postgresql 9.5 /etc/postgresql/9.5/main/postgresql.conf
--/etc/postgresql/9.5/main/postgresql.conf
--listen_addresses = '*'          # what IP address(es) to listen on;
-- data_directory = '/var/lib/postgresql/9.5/main'     # use data in another directory
-- hba_file = '/etc/postgresql/9.5/main/pg_hba.conf'
--             # IPv4 local connections:
--             host    all             all             0.0.0.0/0            md5

-- sudo -i -u postgres
-- ~$ psql
--    alter user postgres password '!postgres@';
-- 재시작 systemctl restart postgresql
--    create user giljabi password '!giljabi-@' superuser;
--    CREATE DATABASE giljabi WITH OWNER giljabi ENCODING 'UTF8' LC_COLLATE='ko_KR.utf8' LC_CTYPE='ko_KR.utf8' TEMPLATE template0;
--    CREATE DATABASE devgiljabi WITH OWNER giljabi ENCODING 'UTF8' LC_COLLATE='ko_KR.utf8' LC_CTYPE='ko_KR.utf8' TEMPLATE template0;
--    \l

select now();

drop table gpsdata;
CREATE TABLE gpsdata (
                         id SERIAL PRIMARY KEY,
                         apiname VARCHAR(16),
                         uuid VARCHAR(36) NOT NULL UNIQUE,
                         userid VARCHAR(255) NOT NULL,
                         createat TIMESTAMP NOT NULL DEFAULT now(),
                         changeat TIMESTAMP NOT NULL DEFAULT now(),
                         wpt INT NOT NULL,
                         trkpt BIGINT NOT NULL,
                         trackname VARCHAR(255) NOT NULL,
                         speed DOUBLE PRECISION NOT NULL,
                         distance DOUBLE PRECISION NOT NULL,
                         fileurl VARCHAR(255),
                         fileext VARCHAR(3),
                         filesize BIGINT default 0,
                         filesizecompress BIGINT default 0,
                         shareflag bool NULL DEFAULT false,
                         readcount int4 DEFAULT 0 NOT NULL,
                         userip VARCHAR(36)
);

-- 트리거를 사용하여 changeat 컬럼을 업데이트
CREATE OR REPLACE FUNCTION update_changeat_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.changeat = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_changeat
    BEFORE UPDATE ON gpsdata
    FOR EACH ROW
    EXECUTE PROCEDURE update_changeat_column(); --v11 이전 PROCEDURE, 이후는 function


insert into
    gpsdata
(apiname, changeat, createat, distance, fileext, filesize, filesizecompress,
 fileurl,
 speed, trackname, trkpt, userid, uuid, wpt)
values
    ('saveGpsdata', now(), now(), 27319.97, 'gpx', 3182, 820,
     'http://3.38.168.206:9000/service/202406/5ccbe0ad-0fb4-4123-8142-a25288d811c9/5ccbe0ad-0fb4-4123-8142-a25288d811c9.gpx',
     2.0, '1718102282', 17,
     'sonnim@giljabi.kr', '5ccbe0ad-0fb4-4123-8142-a25288d811c9', 4)

drop table gpsdataimage;
CREATE TABLE gpsdataimage (
                              id SERIAL PRIMARY KEY,
                              gpsdata_id INT NOT NULL,
                              createat TIMESTAMP NOT NULL DEFAULT now(),
                              changeat TIMESTAMP NOT NULL DEFAULT now(),
                              width INT NOT NULL,
                              height INT NOT NULL,
                              lat DOUBLE PRECISION NOT NULL,
                              lng DOUBLE PRECISION NOT NULL,
                              ele DOUBLE PRECISION NOT NULL,
                              make VARCHAR(255),
                              model VARCHAR(255),
                              originaldatetime VARCHAR(36),
                              fileurl VARCHAR(255) NOT NULL,
                              fileext VARCHAR(8) NOT NULL,
                              filesize BIGINT default 0,
                              filesizecompress BIGINT default 0,
                              originalfname VARCHAR(255) NOT NULL,
                              readcount int4 DEFAULT 0 NOT NULL,
                              userip varchar(36) NULL,
                              readcount int4 DEFAULT 0 NOT NULL,
                              FOREIGN KEY (gpsdata_id) REFERENCES gpsdata(id) ON DELETE CASCADE
);

-- 트리거 생성
CREATE TRIGGER update_changeat
    BEFORE UPDATE ON gpsdataimage
    FOR EACH ROW
    EXECUTE PROCEDURE update_changeat_column();


insert into
    gpsdataimage
(changeat, createat, ele, fileext, filesize, fileurl,
 gpsdata_id, height, lat, lng, make, model, originaldatetime, originalfname, width)
values
    (now(), now(), 243.6218260399784, 'jpeg', 255160, 'http://localhost:9000/service/202406/5ccbe0ad-0fb4-4123-8142-a25288d811c9/df855605-af4f-4d88-b6a3-426e87877811.jpeg',
     1, 768, 38.06739166666667, 127.32834722222222, 'Apple', 'iPhone 12', '2024:06:01 12:22:31', 'IMG_2606.jpeg', 1024)

select *
from gpsdata g;
select *
from gpsdataimage g;

drop table userinfo;
CREATE TABLE userinfo (
                          seqno SERIAL PRIMARY KEY,
                          userid VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(128) NOT NULL,
                          username VARCHAR(32) DEFAULT NULL,
                          level CHAR(2) DEFAULT NULL
);


CREATE TABLE gpselevation (
                              id SERIAL PRIMARY KEY,
                              apiname VARCHAR(16),
                              uuid VARCHAR(36) NOT NULL UNIQUE,
                              fileurl VARCHAR(255) NOT NULL,
                              userid VARCHAR(255) DEFAULT NULL,
                              userip VARCHAR(36) NOT NULL,
                              trackname VARCHAR(128) DEFAULT NULL,
                              createat TIMESTAMP NOT NULL DEFAULT now(),
                              changeat TIMESTAMP NOT NULL DEFAULT now(),
                              transtime INTEGER NOT NULL,
                              wpt SMALLINT NOT NULL,
                              trkpt SMALLINT NOT NULL,
                              readcount int4 DEFAULT 0 NOT NULL
);
CREATE TRIGGER update_changeat
    BEFORE UPDATE ON gpselevation
    FOR EACH ROW
    EXECUTE PROCEDURE update_changeat_column(); --v11 이전 PROCEDURE, 이후는 function

CREATE TABLE gpxrecommend (
                              id SERIAL PRIMARY KEY,
                              trackname VARCHAR(128) DEFAULT NULL,
                              gpxgroup VARCHAR(16),
                              filename VARCHAR(64) NOT NULL UNIQUE,
                              createat TIMESTAMP NOT NULL DEFAULT now(),
                              changeat TIMESTAMP NOT NULL DEFAULT now(),
                              readcnt INTEGER DEFAULT 0
);
CREATE TRIGGER update_changeat
    BEFORE UPDATE ON gpxrecommend
    FOR EACH ROW
    EXECUTE PROCEDURE update_changeat_column(); --v11 이전 PROCEDURE, 이후는 function

-- old giljabi data
CREATE TABLE tcxsharecourses (
                                 fileid SERIAL PRIMARY KEY,
                                 trtime CHAR(14) NOT NULL,
                                 userip VARCHAR(32) NOT NULL,
                                 userno VARCHAR(128) NOT NULL,
                                 filehash VARCHAR(32) NOT NULL,
                                 pcfilename VARCHAR(128) NOT NULL,
                                 pathname VARCHAR(128) NOT NULL,
                                 readcnt INT NOT NULL,
                                 downcnt INT NOT NULL,
                                 recommend INT NOT NULL,
                                 wptcnt INT NOT NULL,
                                 trkcnt INT NOT NULL,
                                 distance INT NOT NULL,
                                 elevation INT NOT NULL,
                                 slope DECIMAL(4,2) NOT NULL,
                                 traveltime INT NOT NULL,
                                 memo TEXT NOT NULL,
                                 flag CHAR(1) DEFAULT NULL
);

COMMENT ON TABLE tcxsharecourses IS '경로공유정보';

COMMENT ON COLUMN tcxsharecourses.fileid IS '파일관리번호';
COMMENT ON COLUMN tcxsharecourses.trtime IS '생성일시';
COMMENT ON COLUMN tcxsharecourses.userip IS '사용자IP';
COMMENT ON COLUMN tcxsharecourses.userno IS '사용자ID';
COMMENT ON COLUMN tcxsharecourses.filehash IS '파일명해시값';
COMMENT ON COLUMN tcxsharecourses.pcfilename IS '파일명';
COMMENT ON COLUMN tcxsharecourses.pathname IS '경로명';
COMMENT ON COLUMN tcxsharecourses.readcnt IS '조회수';
COMMENT ON COLUMN tcxsharecourses.downcnt IS '경로명';
COMMENT ON COLUMN tcxsharecourses.recommend IS '추천';
COMMENT ON COLUMN tcxsharecourses.wptcnt IS '웨이포인트수';
COMMENT ON COLUMN tcxsharecourses.trkcnt IS '트랙수';
COMMENT ON COLUMN tcxsharecourses.distance IS '이동거리';
COMMENT ON COLUMN tcxsharecourses.elevation IS '상승고도';
COMMENT ON COLUMN tcxsharecourses.slope IS '평균경사도';
COMMENT ON COLUMN tcxsharecourses.traveltime IS '시간';
COMMENT ON COLUMN tcxsharecourses.memo IS 'MEMO';
COMMENT ON COLUMN tcxsharecourses.flag IS '상태 플래그';








-- mysql
CREATE DATABASE giljabi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE dev-giljabi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE mytable (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
                         createat timestamp not null default current_timestamp,
                         changeat timestamp not null default on update current_timestamp,
                         wpt INT NOT NULL,
                         trkpt BIGINT NOT NULL,
                         trackname VARCHAR(255) NOT NULL,
                         speed double NOT NULL,
                         distance double NOT NULL,
                         apiname varchar(16),
                         fileurl varchar(255),
                         fileext varchar(3),
                         filesize long,
                        filesizecompress long
);

drop table gpsdataimage;
CREATE TABLE gpsdataimage (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              gpsdata_id INT NOT NULL,
                              createat timestamp not null default current_timestamp,
                              changeat timestamp not null default on update current_timestamp,
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
                              filesize long,
                              originalfname VARCHAR(255) NOT NULL,
                              FOREIGN KEY (gpsdata_id) REFERENCES gpsdata(id) ON DELETE CASCADE
);