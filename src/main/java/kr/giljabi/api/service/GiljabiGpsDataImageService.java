package kr.giljabi.api.service;

import kr.giljabi.api.entity.GiljabiGpsdataImage;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.repository.GiljabiGpxImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
public class GiljabiGpsDataImageService {

    private final GiljabiGpxImageRepository giljabiGpsImageRepository;

    @Autowired
    public GiljabiGpsDataImageService(GiljabiGpxImageRepository giljabiGpsImageRepository) {
        this.giljabiGpsImageRepository = giljabiGpsImageRepository;
    }

    public GiljabiGpsdataImage save(GiljabiGpsdataImage gpsImage, GiljabiGpsdata gpsdata) {
        //gpsdata.addGpsImage(gpsImage);
        return giljabiGpsImageRepository.save(gpsImage);
    }

    public GiljabiGpsdataImage findById(Long id) {
        return giljabiGpsImageRepository.findById(id).orElse(null);
    }

    public ArrayList<GiljabiGpsdataImage> findAllByGpsdata(GiljabiGpsdata gpsdata) {
        return giljabiGpsImageRepository.findAllByGpsdata(gpsdata);
    }
}

