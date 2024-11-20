package kr.giljabi.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.giljabi.api.entity.GiljabiGpsdata;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @Author : eahn.park@gmail.com
 * @Date : 2024.05.28
 * @Description
 */
public interface GiljabiGpsDataRepository extends CrudRepository<GiljabiGpsdata, String> {
    GiljabiGpsdata findByUuid(String uuid);
    boolean existsByUuid(String uuid);
    Optional<GiljabiGpsdata> findById(long id);

    //@Query("SELECT g FROM GiljabiGpsdata g WHERE g.uuid = :uuid AND g.shareflag = false")
    GiljabiGpsdata findByUuidAndShareflagTrue(@Param("uuid") String uuid);

    @Query("SELECT g FROM GiljabiGpsdata g WHERE g.uuid = :uuid AND g.createat >= :tenMinutesAgo")
    GiljabiGpsdata findByApinameAndUuidAndCreateat(@Param("uuid") String uuid,
                                                   @Param("tenMinutesAgo") Timestamp tenMinutesAgo);

    @Query("SELECT g FROM GiljabiGpsdata g WHERE g.createat BETWEEN :startDate AND :endDate " +
            "AND (:trackName IS NULL OR :trackName = '' OR g.trackname = :trackName) " +
            "AND (:useruuid IS NULL OR :useruuid = '' OR g.useruuid = :useruuid) ")
    Page<GiljabiGpsdata> findGpsDataBetweenDatesAndTrackNameByUseruuid(
            Timestamp startDate, Timestamp endDate,
            String trackName, String useruuid, Pageable pageable);

    @Query("SELECT g FROM GiljabiGpsdata g WHERE g.createat BETWEEN :startDate AND :endDate " +
            "AND (:trackName IS NULL OR :trackName = '' OR g.trackname = :trackName) ")
    Page<GiljabiGpsdata> findGpsDataBetweenDatesAndTrackName(
            Timestamp startDate, Timestamp endDate,
            String trackName, Pageable pageable);
}


