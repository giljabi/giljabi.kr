package kr.giljabi.api.service;

import kr.giljabi.api.entity.GpxRecommend;
import kr.giljabi.api.repository.GiljabiGpxRecommendRepository;
import kr.giljabi.api.response.Mountain100;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GiljabiGpxRecommendService {

    private final GiljabiGpxRecommendRepository giljabiGpxRecommendRepository;

    @Autowired
    public GiljabiGpxRecommendService(GiljabiGpxRecommendRepository giljabiGpxRecommendRepository) {
        this.giljabiGpxRecommendRepository = giljabiGpxRecommendRepository;
    }

    public Optional<GpxRecommend> findById(Long id) {
        return giljabiGpxRecommendRepository.findById(id);
    }

    public List<GpxRecommend> findByTrackname(String trackName) {
        return (List<GpxRecommend>) giljabiGpxRecommendRepository.findByTrackname(trackName);
    }

    public List<Mountain100> findTrackNamesByGpxGroup(String gpxgroup) {
        return giljabiGpxRecommendRepository.findTrackNamesByGpxGroup(gpxgroup);
    }

    public List<String> findByGpxgroupAndTracknameOrderByFilename(String gpxgroup, String trackname) {
        //필요한 정보만 조립
        List<GpxRecommend> trackNames = giljabiGpxRecommendRepository.findByGpxgroupAndTracknameOrderByFilename(gpxgroup, trackname);
        return trackNames.stream()
                .map(GpxRecommend::getFilename)
                .collect(Collectors.toList());
    }
}
