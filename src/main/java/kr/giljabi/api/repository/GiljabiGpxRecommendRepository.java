package kr.giljabi.api.repository;

import kr.giljabi.api.entity.GpxRecommend;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;


/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.28
 * @Description
 */
public interface GiljabiGpxRecommendRepository extends CrudRepository<GpxRecommend, Long> {
    List<GpxRecommend> findByTrackname(String trackName);
}
