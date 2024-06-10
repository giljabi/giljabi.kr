package kr.giljabi.api.repository;

import kr.giljabi.api.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    UserInfo findByUserid(String userId);
}
