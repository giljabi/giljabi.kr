package kr.giljabi.api.auth;

import kr.giljabi.api.utils.MyHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {
    
    @Override
	public void onAuthenticationFailure(HttpServletRequest request,
					HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
		log.info("{} {} LoginFailureHandler",
				MyHttpUtils.getClientIp(request),
				request.getRequestURI());
		request.setAttribute("errorMsg", exception.getMessage());
		RequestDispatcher dispatcher = request.getRequestDispatcher("/v2/login");
		dispatcher.forward(request, response);
	}
}



