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

	@Override
	public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
		UserInfo user = userService.selectOneByUserId(userid);
		if(user == null) {
			throw new UsernameNotFoundException("empty user");
		}
		return UserPrincipal.build(user);
	}
}