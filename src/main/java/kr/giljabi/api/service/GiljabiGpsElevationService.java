package kr.giljabi.api.service;

import kr.giljabi.api.entity.GpsElevation;
import kr.giljabi.api.repository.GiljabiGpsElevationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GiljabiGpsElevationService {
    private final GiljabiGpsElevationRepository giljabiGpsElevationRepository;

    @Autowired
    public GiljabiGpsElevationService(GiljabiGpsElevationRepository giljabiGpsElevationRepository) {
        this.giljabiGpsElevationRepository = giljabiGpsElevationRepository;
    }

    public void saveElevation(GpsElevation gpsElevation) {
        giljabiGpsElevationRepository.save(gpsElevation);
    }
}
