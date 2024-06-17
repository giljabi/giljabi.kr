package kr.giljabi.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
//@RequestMapping("/user")
public class UserController {

    @RequestMapping("/login")
    public String loginForm(){
        log.info("loginForm");
        return "login";
    }

}
