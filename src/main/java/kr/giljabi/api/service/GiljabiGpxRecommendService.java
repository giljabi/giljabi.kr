package kr.giljabi.api.service;

import kr.giljabi.api.entity.GpxRecommend;
import kr.giljabi.api.repository.GiljabiGpxRecommendRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

}
