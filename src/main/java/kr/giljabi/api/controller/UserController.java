package kr.giljabi.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@Slf4j
//@RequestMapping("/user")
public class UserController {

    @Autowired
    private HttpSession session;

    @RequestMapping("/login")
    public String loginForm(){
        log.info("loginForm");
        return "login";
    }
/*
    @RequestMapping("/logout")
    public String logout(){
        session.invalidate();

        // 로그아웃 메시지 로그
        log.info("logout");

        // 로그아웃 페이지로 리다이렉트
        return "/login";
        //return "redirect:/login";
    }*/

    @RequestMapping("/manager/giljabi2")
    public String goManagerGiljabi2(){
        log.info("giljabi2");
        return "manager/giljabi2";
    }

}
