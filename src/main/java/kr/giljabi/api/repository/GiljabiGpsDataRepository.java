package kr.giljabi.api.repository;

import kr.giljabi.api.entity.GiljabiGpsdata;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @Author : eahn.park@gmail.com
 * @Date : 2024.05.28
 * @Description
 */
public interface GiljabiGpsDataRepository extends CrudRepository<GiljabiGpsdata, String> {
    GiljabiGpsdata findByUuid(String uuid);
    Optional<GiljabiGpsdata> findById(long id);

    //@Query("SELECT g FROM GiljabiGpsdata g WHERE g.uuid = :uuid AND g.shareflag = false")
    GiljabiGpsdata findByUuidAndShareflagTrue(@Param("uuid") String uuid);

    @Query("SELECT g FROM GiljabiGpsdata g WHERE g.uuid = :uuid AND g.createat >= :tenMinutesAgo")
    GiljabiGpsdata findByApinameAndUuidAndCreateat(@Param("uuid") String uuid,
                                                   @Param("tenMinutesAgo") Timestamp tenMinutesAgo);

}
