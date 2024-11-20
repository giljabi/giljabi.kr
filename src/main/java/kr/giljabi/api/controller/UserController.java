package kr.giljabi.api.controller;

import kr.giljabi.api.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @RequestMapping("/v2/login")
    public String loginForm(){
        log.info("loginForm");
        return "login";
    }

    @RequestMapping("/v2/logout")
    public String logout(HttpSession session){
        session.removeAttribute("token");
        session.invalidate();

        // 로그아웃 메시지 로그
        log.info("logout");

        // 로그아웃 페이지로 리다이렉트
        //return "/login";
        return "redirect:/v2";
    }

    @RequestMapping("/v2/manager/giljabi2-admin")
    public String goManagerGiljabi2(){
        log.info("giljabi2");
        return "v2/manager/giljabi2-admin";
    }

    @GetMapping("/v2/generate-uuid")
    public String generateUuid(HttpServletRequest request, HttpServletResponse response) {
        String uuid = CommonUtils.getCookieValue(request, CommonUtils.GILJABI_UUID);        // UUID 생성
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();

            // 쿠키 생성 및 설정
            Cookie uuidCookie = new Cookie(CommonUtils.GILJABI_UUID, uuid);
            uuidCookie.setHttpOnly(true); // JavaScript에서 접근 불가 (보안 강화)
            uuidCookie.setPath("/");
            //uuidCookie.setMaxAge(356 * 24 * 60 * 60); // 쿠키 만료 시간: 365일
            response.addCookie(uuidCookie);
        }

        return "redirect:/";
    }
}


