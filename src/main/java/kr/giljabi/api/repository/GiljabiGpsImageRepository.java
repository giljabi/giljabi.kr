package kr.giljabi.api.repository;

import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.entity.GiljabiGpsdataImage;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.28
 * @Description
 */
public interface GiljabiGpsImageRepository extends CrudRepository<GiljabiGpsdataImage, String> {
    Optional<GiljabiGpsdataImage> findByGpsdata(GiljabiGpsdata gpsdata);
    ArrayList<GiljabiGpsdataImage> findAllByGpsdata(GiljabiGpsdata gpsdata);
}
