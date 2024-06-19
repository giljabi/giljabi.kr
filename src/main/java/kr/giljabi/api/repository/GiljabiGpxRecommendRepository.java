package kr.giljabi.api.repository;

import kr.giljabi.api.entity.GpxRecommend;
import kr.giljabi.api.response.Mountain100;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.28
 * @Description
 */
public interface GiljabiGpxRecommendRepository extends CrudRepository<GpxRecommend, Long> {
    List<GpxRecommend> findByTrackname(String trackName);

    @Query("SELECT new kr.giljabi.api.response.Mountain100(gr.trackname, gr.trackkorean) FROM GpxRecommend gr WHERE gr.gpxgroup = :gpxgroup GROUP BY gr.trackname, gr.trackkorean ORDER BY gr.trackkorean")
    List<Mountain100> findTrackNamesByGpxGroup(@Param("gpxgroup") String gpxgroup);

    List<GpxRecommend> findByGpxgroupAndTracknameOrderByFilename(String gpxgroup, String trackname);

}
