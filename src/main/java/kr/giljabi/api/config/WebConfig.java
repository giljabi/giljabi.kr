package kr.giljabi.api.config;

//import kr.giljabi.api.config.defaultsession.DefaultSessionFilter;
//import kr.giljabi.api.config.defaultsession.DefaultSessionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins("*");
	}

	/**
	 nginx:
	 proxy_set_header X-Real-IP $remote_addr;
	 proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
	 proxy_set_header Host $host;
	 proxy_set_header X-Forwarded-Proto $scheme;

	 yml:
	 server.forward-headers-strategy: native  #nginx 사용시 remote address(ip) 가져오기 위해 설정

	 * @return
	 */
	@Bean
	public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
		FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>(new ForwardedHeaderFilter());
		filterRegistrationBean.setOrder(0);
		return filterRegistrationBean;
	}

	/**
	 * 세션이 없는 경우 강제로 세션을 생성하는 필터
	 * @return
	 */
/*
	@Bean
	public FilterRegistrationBean<DefaultSessionFilter> defaultSessionFilter() {
		FilterRegistrationBean<DefaultSessionFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new DefaultSessionFilter());
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}

	@Autowired
	private DefaultSessionInterceptor defaultSessionInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(defaultSessionInterceptor).addPathPatterns("/**");
	}
	// 세션이 없는 경우 강제로 세션을 생성하는 필터 끝
*/

}
