package kr.giljabi.api.repository;

import kr.giljabi.api.entity.GiljabiGpsdata;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.28
 * @Description
 */
public interface GiljabiGpsDataRepository extends CrudRepository<GiljabiGpsdata, String> {
    GiljabiGpsdata findByUuid(String uuid);
    Optional<GiljabiGpsdata> findById(long id);
}
