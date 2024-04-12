package kr.giljabi.api.service;

import kr.giljabi.api.entity.ShareCourses;
import kr.giljabi.api.repository.ShareCoursesRepository;
import kr.giljabi.api.response.XmlShareResponse;
import kr.giljabi.api.utils.MyHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Service
public class ShareCoursesService {
    private final ShareCoursesRepository shareCoursesRepository;

    @Value("${giljabi.xmlshare.path}")
    private String xmlSharePath;

    @Autowired
    public ShareCoursesService(ShareCoursesRepository shareCoursesRepository) {
        this.shareCoursesRepository = shareCoursesRepository;
    }

    public Optional<XmlShareResponse> findByFileHash(String fileHashId) {
        Optional<ShareCourses> shareCourses = shareCoursesRepository.findByFileHash(fileHashId);
        try {
            if (shareCourses.isPresent()) {
                XmlShareResponse xmlShareResponse = new XmlShareResponse();

                String filaPath = String.format("%s/%s/%s.tcx",
                        xmlSharePath,
                        shareCourses.get().getPathName(),
                        shareCourses.get().getFileHash());
                byte[] xmlFile = Files.readAllBytes(Paths.get(filaPath));
                String xmlData = Base64Utils.encodeToString(MyHttpUtils.byteCompress(xmlFile));

                xmlShareResponse.setXmlData(xmlData);
                xmlShareResponse.setTrackName(shareCourses.get().getPathName());
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
}
