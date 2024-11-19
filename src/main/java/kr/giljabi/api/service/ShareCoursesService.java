package kr.giljabi.api.service;

import kr.giljabi.api.entity.TcxShareCourses;
import kr.giljabi.api.repository.ShareCoursesRepository;
import kr.giljabi.api.response.XmlShareResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Service
public class ShareCoursesService {
    private final ShareCoursesRepository shareCoursesRepository;
    private final MinioService minioService;

    @Value("${giljabi.xmlshare.path}")
    private String xmlSharePath;

    @Value("${minio.bucketPublic}")
    private String bucketPublic;

    @Autowired
    public ShareCoursesService(ShareCoursesRepository shareCoursesRepository
            , MinioService minioService) {
        this.shareCoursesRepository = shareCoursesRepository;
        this.minioService = minioService;
    }

    public Optional<XmlShareResponse> findByFileHash(String fileHashId) {
        try {
            Optional<TcxShareCourses> shareCourses = shareCoursesRepository.findByFileHash(fileHashId);
            if (shareCourses.isPresent()) {
                String filePath = String.format("share/%s/%s.tcx.lz",
                        shareCourses.get().getPathName(),
                        shareCourses.get().getFileHash());

                XmlShareResponse xmlShareResponse = new XmlShareResponse();
                //String xmlData = minioService.getObjectByString(bucketPublic, filePath);
                //xmlShareResponse.setXmlData(xmlData);
                xmlShareResponse.setUuid(fileHashId);
                xmlShareResponse.setTrackName(shareCourses.get().getPcFileName());
                xmlShareResponse.setFileType("tcx");
                return Optional.of(xmlShareResponse);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    //과거 공유파일은 tcx로 저장되어 있음
    public String readTcxFileAsString(String pathName, String hashName) throws IOException {
        String fileFullName = String.format("%s/%s/%s.tcx",
                xmlSharePath, pathName, hashName);

        File file = new File(fileFullName);
        byte[] binaryData = FileCopyUtils.copyToByteArray(file);
        return new String(binaryData, StandardCharsets.UTF_8);
    }
}

