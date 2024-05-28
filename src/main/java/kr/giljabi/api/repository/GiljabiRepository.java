package kr.giljabi.api.repository;

import kr.giljabi.api.entity.GiljabiGpsdata;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.28
 * @Description
 */
public interface GiljabiRepository extends JpaRepository<GiljabiGpsdata, String> {
}
