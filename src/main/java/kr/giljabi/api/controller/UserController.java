package kr.giljabi.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

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

}



