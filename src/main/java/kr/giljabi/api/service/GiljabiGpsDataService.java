package kr.giljabi.api.service;

import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.repository.GiljabiGpsDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class GiljabiGpsDataService {

    private final GiljabiGpsDataRepository giljabiGpsDataRepository;

    @Autowired
    public GiljabiGpsDataService(GiljabiGpsDataRepository giljabiGpsDataRepository) {
        this.giljabiGpsDataRepository = giljabiGpsDataRepository;
    }

    public GiljabiGpsdata saveGpsdata(GiljabiGpsdata giljabiGpsdata) {
        return giljabiGpsDataRepository.save(giljabiGpsdata);
    }

    public Optional<GiljabiGpsdata> findById(Long id) {
        return giljabiGpsDataRepository.findById(id);
    }

    public GiljabiGpsdata findByUuid(String uuid) {
        return giljabiGpsDataRepository.findByUuid(uuid);
    }

    public GiljabiGpsdata findByUuidAndShareflagTrue(String uuid) {
        return giljabiGpsDataRepository.findByUuidAndShareflagTrue(uuid);
    }

}
