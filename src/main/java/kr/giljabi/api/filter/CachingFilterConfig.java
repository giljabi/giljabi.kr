package kr.giljabi.api.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CachingFilterConfig {

    @Bean
    public FilterRegistrationBean<CachingBodyFilter> CachingRequestBodyFilter() {
        FilterRegistrationBean<CachingBodyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CachingBodyFilter());
        registrationBean.addUrlPatterns("/api/**"); // URL 패턴, api이하 모든것에 필터 적용
        registrationBean.setEnabled(true); // 필터 활성화
        return registrationBean;
    }

}