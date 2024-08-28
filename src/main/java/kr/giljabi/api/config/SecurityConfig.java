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
                .antMatchers("/css/**", "/images/**", "/js/**",
                        "/map/**", "/poieditor/**", "/util/**", "/vendor/**",
                        "/user/**")
                .permitAll()

                // 로그인한 사용자만 접근 가능
                .antMatchers("/manager/giljabi2")
                .authenticated();
                /*
                //.antMatchers("/hello/**").permitAll() //모든 이미지, 파일들...
                //.requestMatchers(request -> "127.0.0.1".equals(request.getRemoteAddr()) || "::1".equals(request.getRemoteAddr()))
                //.permitAll()
                //.anyRequest().authenticated()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/**").permitAll()
                //.antMatchers("/api/**").permitAll()
                //.anyRequest().authenticated(); //인증 필요시
                 .anyRequest().permitAll(); //모든 url 접속허용
*/

        http
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/authenticate")
                .successHandler(this.loginSuccessHandler)
                .failureHandler(this.loginFailureHandler)
                .usernameParameter("userid")
                .passwordParameter("password")
                .permitAll();

        http
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logout")
                .invalidateHttpSession(true)
                .permitAll();

        http
                .sessionManagement()
                .maximumSessions(1)
                .expiredUrl("/login?expired")
                .maxSessionsPreventsLogin(true)
                .and()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .invalidSessionUrl("/login");


    }
}