package kr.giljabi.api.service;

import kr.giljabi.api.entity.ShareCourses;
import kr.giljabi.api.repository.ShareCoursesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ShareCoursesService {
    private final ShareCoursesRepository shareCoursesRepository;

    @Autowired
    public ShareCoursesService(ShareCoursesRepository shareCoursesRepository) {
        this.shareCoursesRepository = shareCoursesRepository;
    }

    public Optional<ShareCourses> findByFileHash(String fileHashId) {
        return shareCoursesRepository.findByFileHash(fileHashId);
    }
}
