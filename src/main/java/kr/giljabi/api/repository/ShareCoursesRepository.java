package kr.giljabi.api.repository;

import kr.giljabi.api.entity.ClientInfo;
import kr.giljabi.api.entity.ShareCourses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareCoursesRepository extends JpaRepository<ShareCourses, String> {
    Optional<ShareCourses> findByFileHash(String fileHashId);
}

