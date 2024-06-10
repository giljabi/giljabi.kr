package kr.giljabi.api.auth;


import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserService userService;
	private UserInfo userInfo;
	@Override
	public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
		userInfo = userService.selectOneByUserId(userid);
		if(userInfo == null) {
			throw new UsernameNotFoundException("empty user");
		}
		return UserPrincipal.build(userInfo);
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}
}