package kr.giljabi.api.service;

import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.repository.GiljabiGpsDataRepository;
import kr.giljabi.api.response.GiljabiResponseGpsdataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GiljabiGpsDataService {

    private final GiljabiGpsDataRepository giljabiGpsDataRepository;

    @Autowired
    public GiljabiGpsDataService(GiljabiGpsDataRepository giljabiGpsDataRepository) {
        this.giljabiGpsDataRepository = giljabiGpsDataRepository;
    }

    public GiljabiGpsdata save(GiljabiGpsdata giljabiGpsdata) {
        return giljabiGpsDataRepository.save(giljabiGpsdata);
    }

    public Optional<GiljabiGpsdata> findById(Long id) {
        return giljabiGpsDataRepository.findById(id);
    }

    public GiljabiGpsdata findByUuid(String uuid) {
        return giljabiGpsDataRepository.findByUuid(uuid);
    }

    public GiljabiGpsdata findByUuidAndShareflagTrue(String uuid) {
        return giljabiGpsDataRepository.findByUuidAndShareflagTrue(uuid);
    }
    public GiljabiGpsdata findByApinameAndUuidAndCreateat(String uuid) {
        Timestamp tenMinutesAgo = Timestamp.from(Instant.now().minusSeconds(600)); // 10 minutes ago

        GiljabiGpsdata gpsdata = giljabiGpsDataRepository.findByApinameAndUuidAndCreateat(uuid, tenMinutesAgo);
/*
        //DB에서 삭제하지는 않고 object만 삭제
        if (gpsdata != null) {
            giljabiGpsDataRepository.delete(gpsdata);
        }*/
        return gpsdata;
    }


    public Page<GiljabiGpsdata> findGpsDataBetweenDatesAndTrackName(
            String trackName,
            String useruuid,
            boolean selfCheck,
            Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        Timestamp startDate = Timestamp.valueOf(oneMonthAgo);
        Timestamp endDate = Timestamp.valueOf(now);

        Page<GiljabiGpsdata> pageContents = null;

        //본인것만 보기
        if(selfCheck)
            pageContents = giljabiGpsDataRepository.findGpsDataBetweenDatesAndTrackNameByUseruuid(
                        startDate, endDate, trackName, useruuid, pageable);
        else
            pageContents = giljabiGpsDataRepository.findGpsDataBetweenDatesAndTrackName(
                    startDate, endDate, trackName, pageable);

        return pageContents;
    }

    public int updateShareFlagByUuid(String uuid, boolean shareFlag) {
        return giljabiGpsDataRepository.updateShareFlagByUuid(uuid, shareFlag);
    }
}


