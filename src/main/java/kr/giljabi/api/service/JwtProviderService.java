package kr.giljabi.api.service;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import kr.giljabi.api.entity.UserInfo;
import kr.giljabi.api.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
public class JwtProviderService {
    private final JwtProvider jwtProvider;

    @Autowired
    public JwtProviderService(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * session정보가 있거나 없거나 반드시 리턴해야 함
     * @param request
     * @return
     */
    public UserInfo getSessionByUserinfo(HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();
        try {
            HttpSession session = request.getSession();
            Jws<Claims> claims = jwtProvider.getClaims((String) session.getAttribute("token"));
            userInfo = new Gson().fromJson((String) claims.getBody().get("userinfo"), UserInfo.class);
            userInfo.setUserid(claims.getBody().getSubject());  //userid(email)
        } catch(Exception e) {
            userInfo.setLevel("00");
            userInfo.setUserid("sonnim@giljabi.kr");
            userInfo.setUsername("손님");
        }
        return userInfo;
    }
}
