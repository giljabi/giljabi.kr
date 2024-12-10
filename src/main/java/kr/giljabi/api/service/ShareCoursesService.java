package kr.giljabi.api.service;

import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.entity.TcxShareCourses;
import kr.giljabi.api.repository.ShareCoursesRepository;
import kr.giljabi.api.response.XmlShareResponse;
import kr.giljabi.api.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * @Author : eahn.park@gmail.com
 * 블로그등에 링크된 내용은 삭제하지 않고 사용함
 * https://giljabi.kr/gpx2tcx.html?fileid=8a68fb2ecc4ae72ab097dc7fff0b9296
 */
@Slf4j
@Service
public class ShareCoursesService {
    private final ShareCoursesRepository shareCoursesRepository;
    private final GiljabiGpsDataService gpsService;


    @Value("${giljabi.xmlshare.path}")
    private String xmlSharePath;

    @Value("${giljabi.gpx.path}")
    private String xmlGpxPath;


    @Autowired
    public ShareCoursesService(ShareCoursesRepository shareCoursesRepository,
                               GiljabiGpsDataService gpsService) {
        this.shareCoursesRepository = shareCoursesRepository;
        this.gpsService = gpsService;
    }

    //과거 공유된 파일은 tcx로 저장되어 있음
    public Optional<XmlShareResponse> findByFileHash(String fileHashId) throws Exception {
        Optional<TcxShareCourses> shareCourses = shareCoursesRepository.findByFileHash(fileHashId);
        if (shareCourses.isPresent()) {
            String filePath = String.format("%s/%s/%s.tcx.lz",
                    xmlSharePath,
                    shareCourses.get().getPathName(),
                    shareCourses.get().getFileHash());

            XmlShareResponse xmlShareResponse = new XmlShareResponse();
            String xmlData = FileUtils.fileReaderByText(filePath);
            xmlShareResponse.setXmlData(xmlData);
            xmlShareResponse.setUuid(fileHashId);
            xmlShareResponse.setTrackName(shareCourses.get().getPcFileName());
            xmlShareResponse.setFileType("tcx");
            return Optional.of(xmlShareResponse);
        } else {
            return Optional.empty();
        }
    }

    public Optional<XmlShareResponse> findByUuidFromGpxdata(String fileid) throws Exception {
        Optional<XmlShareResponse> response = null;

        //조회수 증가
        gpsService.incrementReadcountByFileHash(fileid);

        //Optional을 사용해야 하나...
        GiljabiGpsdata gpsdata = gpsService.findByUuid(fileid);
        XmlShareResponse xml = new XmlShareResponse();
        String filePath = String.format("%s%s/%s",
                xmlGpxPath,
                gpsdata.getFileurl(),
                gpsdata.getUuid());

        String xmlData = FileUtils.fileReaderByText(filePath);

        xml.setXmlData(xmlData);
        xml.setUuid(gpsdata.getUuid());
        xml.setTrackName(gpsdata.getTrackname());
        xml.setFileType(gpsdata.getFileext());
        xml.setFileId(fileid);
        response = Optional.of(xml);

        return response;
    }
}



