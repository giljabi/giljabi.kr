package kr.giljabi.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
//@RequestMapping("/user")
public class UserController {

    @RequestMapping("/login")
    public String loginForm(){
        return "login";
    }

}
