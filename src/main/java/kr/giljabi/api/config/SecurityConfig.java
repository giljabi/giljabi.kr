package kr.giljabi.api.config;

import kr.giljabi.api.auth.LoginFailureHandler;
import kr.giljabi.api.auth.LoginSuccessHandler;
import kr.giljabi.api.auth.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsServiceImpl userDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .xssProtection().block(true)
                .and()
                .frameOptions().deny()
                .and()
                .cors()
                .and()
                .csrf().disable()
                .authorizeRequests()

                // 모든 요청은 인증 없이 접근 가능
                .antMatchers("/", "/v2/**", "/css/**", "/images/**", "/js/**",
                        "/map/**", "/poieditor/**", "/util/**", "/vendor/**",
                        "/authenticate")
                .permitAll()
                .antMatchers().permitAll()

                // 로그인한 사용자만 접근 가능
                .antMatchers("/manage-js/**", "/manage/**")
                .authenticated();

        http
                .formLogin()
                .loginPage("/v2/login")   // /v2/login 로그인 화면으로 보내지 않음
                .loginProcessingUrl("/authenticate")
                .successHandler(this.loginSuccessHandler)
                .failureHandler(this.loginFailureHandler)
                .usernameParameter("userId")
                .passwordParameter("password")
                .permitAll();

        http
                .logout()
                .logoutUrl("/v2/logout")
                //.logoutSuccessUrl("/v2/giljabi2.html") login.html에서 redirect로 처리
                .invalidateHttpSession(true)
                .permitAll();

        http
                .sessionManagement()
                .maximumSessions(1)
                .expiredUrl("/v2/login?expired")
                .maxSessionsPreventsLogin(true)
                .and()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .invalidSessionUrl("/v2/giljabi2.html");
    }
}

