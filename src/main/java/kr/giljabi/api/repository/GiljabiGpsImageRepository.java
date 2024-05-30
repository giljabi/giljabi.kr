package kr.giljabi.api.repository;

import kr.giljabi.api.entity.GiljabiGpsdataImage;
import org.springframework.data.repository.CrudRepository;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.28
 * @Description
 */
public interface GiljabiGpsImageRepository extends CrudRepository<GiljabiGpsdataImage, String> {
}
