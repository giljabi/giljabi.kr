package kr.giljabi.api.config.defaultsession;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.06.21
 * @Description
 * DefaultSessionInterceptor.java:
 * 이 인터셉터는 요청을 처리하기 전에 세션이 존재하는지 확인합니다.
 * 그렇지 않은 경우 하나를 만들고 기본 속성을 설정합니다.
 * 세션이 존재하지만 기본 속성이 설정되지 않은 경우 기본 속성을 설정합니다.
 *
 *
token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzb25uaW1AZ2lsamFiaS5rciIsInVzZXJpbmZvIjoie1wibGV2ZWxcIjpcIjAwXCIsXCJ1c2VybmFtZVwiOlwi7IaQ64uYXCJ9IiwiaWF0IjoxNzE4OTMzODQ4LCJleHAiOjE3MTk1Mzg2NDh9.IMleD19t0a_WyQWKYOo2vOY8unicjdPzSNqzgOuiszk

{
"sub": "sonnim@giljabi.kr",
"userinfo": "{\"level\":\"00\",\"username\":\"손님\"}",
"iat": 1718933848,
"exp": 1719538648
}

 */
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class DefaultSessionInterceptor implements HandlerInterceptor {
    private static final String DEFAULT_NAME = "token";
    private static final String DEFAULT_VALUE = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzb25uaW1AZ2lsamFiaS5rciIsInVzZXJpbmZvIjoie1wibGV2ZWxcIjpcIjAwXCIsXCJ1c2VybmFtZVwiOlwi7IaQ64uYXCJ9IiwiaWF0IjoxNzE4OTMzODQ4LCJleHAiOjE3MTk1Mzg2NDh9.IMleD19t0a_WyQWKYOo2vOY8unicjdPzSNqzgOuiszk";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if (session == null) {
            session = request.getSession(true);
            session.setAttribute(DEFAULT_NAME, DEFAULT_VALUE);
        } else if (session.getAttribute(DEFAULT_NAME) == null) {
            session.setAttribute(DEFAULT_NAME, DEFAULT_VALUE);
        }

        return true;
    }
}