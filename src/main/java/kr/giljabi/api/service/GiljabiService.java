package kr.giljabi.api.service;

import kr.giljabi.api.entity.GiljabiGpsdataImage;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.repository.GiljabiGpsDataRepository;
import kr.giljabi.api.repository.GiljabiGpsImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class GiljabiService {

    private final GiljabiGpsDataRepository giljabiGpsDataRepository;
    private final GiljabiGpsImageRepository giljabiGpsImageRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    public GiljabiService(GiljabiGpsDataRepository giljabiGpsDataRepository,
                          GiljabiGpsImageRepository giljabiGpsImageRepository1) {
        this.giljabiGpsDataRepository = giljabiGpsDataRepository;
        this.giljabiGpsImageRepository = giljabiGpsImageRepository1;
    }

    public GiljabiGpsdata saveGpsdata(GiljabiGpsdata giljabiGpsdata) {
        return giljabiGpsDataRepository.save(giljabiGpsdata);
    }
    public Optional<GiljabiGpsdata> findById(Long gpsdataId) {
        return giljabiGpsDataRepository.findById(gpsdataId);
    }

    public GiljabiGpsdata findByGpsdataUuid(String gpsdataUuid) {
        return giljabiGpsDataRepository.findByUuid(gpsdataUuid);
    }
    public GiljabiGpsdataImage saveGpsImage(GiljabiGpsdataImage gpsImage, GiljabiGpsdata gpsdata) {
        gpsdata.addGpsImage(gpsImage);
        return giljabiGpsImageRepository.save(gpsImage);
    }
}
