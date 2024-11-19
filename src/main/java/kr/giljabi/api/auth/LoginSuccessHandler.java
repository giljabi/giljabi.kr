package kr.giljabi.api.auth;

import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.jwt.JwtProvider;
import kr.giljabi.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

	private HttpSession session;

	private RedirectStrategy redirectStratgy = new DefaultRedirectStrategy();

	@Autowired
	private final UserService userService;

	@Autowired
	private final JwtProvider jwtProvider;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
										HttpServletResponse response,
										Authentication authentication) throws IOException {
		request.getSession().invalidate();
		session = request.getSession(true);
		UserPrincipal user =  (UserPrincipal) authentication.getPrincipal();

		//사용자 ID를 이용하여 필요한 정보를 조회하여 세션에 토큰으로 저장
		UserInfo userInfo = userService.selectOneByUserId(user.getUsername());
		String token = jwtProvider.generateJwtToken(userInfo);

		this.session.setAttribute("token", token);
		this.redirectStratgy.sendRedirect(request, response, "/v2/manager/giljabi2-admin");
	}

}

