package kr.giljabi.api.config.defaultsession;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.06.21
 * @Description
 * 세션이 없는 경우 강제로 세션을 생성하는 필터, 이유는 ....
DefaultSessionFilter.java: 이 필터는 세션이 존재하는지 확인합니다.
그렇지 않은 경우 하나를 만들고 기본 속성을 설정합니다.
세션이 존재하지만 기본 속성이 설정되지 않은 경우 기본 속성을 설정합니다.
 */
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class DefaultSessionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            session = httpRequest.getSession(true);
            session.setAttribute("defaultAttribute", "defaultValue");
        } else if (session.getAttribute("defaultAttribute") == null) {
            session.setAttribute("defaultAttribute", "defaultValue");
        }

        chain.doFilter(httpRequest, httpResponse);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}