package kr.giljabi.api.service;

import kr.giljabi.api.entity.GpxRecommend;
import kr.giljabi.api.repository.GiljabiGpsRecommendRepository;
import kr.giljabi.api.response.Forest100;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GiljabiGpsRecommendService {

    private final GiljabiGpsRecommendRepository giljabiGpsRecommendRepository;

    @Autowired
    public GiljabiGpsRecommendService(GiljabiGpsRecommendRepository giljabiGpsRecommendRepository) {
        this.giljabiGpsRecommendRepository = giljabiGpsRecommendRepository;
    }

    public Optional<GpxRecommend> findById(Long id) {
        return giljabiGpsRecommendRepository.findById(id);
    }

    public List<GpxRecommend> findByTrackname(String trackName) {
        return (List<GpxRecommend>) giljabiGpsRecommendRepository.findByTrackname(trackName);
    }

    public List<Forest100> findTrackNamesByGpxGroup(String gpxgroup) {
        return giljabiGpsRecommendRepository.findTrackNamesByGpxGroup(gpxgroup);
    }

    public List<String> findByGpxgroupAndTracknameOrderByFilename(String gpxgroup, String trackname) {
        //필요한 정보만 조립
        List<GpxRecommend> trackNames = giljabiGpsRecommendRepository.findByGpxgroupAndTracknameOrderByFilename(gpxgroup, trackname);
        return trackNames.stream()
                .map(GpxRecommend::getFilename)
                .collect(Collectors.toList());
    }
}

