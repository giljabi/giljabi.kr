package kr.giljabi.api.repository;

import kr.giljabi.api.entity.TcxShareCourses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareCoursesRepository extends JpaRepository<TcxShareCourses, String> {
    Optional<TcxShareCourses> findByFileHash(String fileHashId);
}

