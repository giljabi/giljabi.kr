package kr.giljabi.api.service;

import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserInfoRepository userInfoRepository;

    public UserInfo selectOneByUserId(String userId){
        return userInfoRepository.findByUserid(userId);
    }
/*
    @Transactional
    public int insertUser(InsertUserRequest request, UserPrincipal loginUser){

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        User user = User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .userNm(request.getUserNm())
                .phone(request.getPhone())
                .useFlag(request.getUseFlag())
                .createdBy(loginUser.getUsername()).build();

        userRepository.insertUser(user);

        List<Long> authGroupIdList = request.getAuthGroupIdList();

        int result = this.insertAuthGroupListByUserId(user.getUserId(), authGroupIdList, loginUser);

        return result;
    }

    @Transactional
    public int updateUser(UpdateUserRequest request, UserPrincipal loginUser){

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        User.UserBuilder userBuilder = User.builder()
                .userId(request.getUserId())
                .userNm(request.getUserNm())
                .phone(request.getPhone())
                .useFlag(request.getUseFlag())
                .updatedBy(loginUser.getUsername());

        if(StringUtils.hasText(request.getPassword())){
            userBuilder.password(passwordEncoder.encode(request.getPassword()));
        }

        User user = userBuilder.build();

        userRepository.updateUser(user);

        List<Long> authGroupIdList = request.getAuthGroupIdList();

        int result = this.updateAuthGroupListByUserId(user.getUserId(), authGroupIdList, loginUser);

        return result;
    }
*/
}
