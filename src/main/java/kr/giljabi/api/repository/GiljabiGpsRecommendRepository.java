package kr.giljabi.api.repository;

import kr.giljabi.api.entity.GpxRecommend;
import kr.giljabi.api.response.Forest100;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * @Author : eahn.park@gmail.com
 * @Date : 2024.05.28
 * @Description
 */
public interface GiljabiGpsRecommendRepository extends CrudRepository<GpxRecommend, Long> {
    List<GpxRecommend> findByTrackname(String trackName);

    @Query("SELECT new kr.giljabi.api.response.Forest100(gr.trackname, gr.trackkorean) FROM GpxRecommend gr WHERE gr.gpxgroup = :gpxgroup GROUP BY gr.trackname, gr.trackkorean ORDER BY gr.trackkorean")
    List<Forest100> findTrackNamesByGpxGroup(@Param("gpxgroup") String gpxgroup);

    List<GpxRecommend> findByGpxgroupAndTracknameOrderByFilename(String gpxgroup, String trackname);

}

