package kr.giljabi.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kr.giljabi.api.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtProvider {
	@Value("${jwt.accessTokenSecret}")
	private String accessTokenSecret;

	@Value("${jwt.accessTokenExpiration}")
	private long accessTokenExpiration;

	public String generateJwtToken(UserInfo userInfo) throws JsonProcessingException {
		SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessTokenSecret));
		Date expiration = new Date((new Date()).getTime() + accessTokenExpiration);

		// 클레임 생성
		Map<String, Object> claims = new HashMap<>();
		claims.put("level", userInfo.getLevel());
		claims.put("username", userInfo.getUsername());

		// ObjectMapper를 사용하여 JSON 문자열로 변환
		ObjectMapper objectMapper = new ObjectMapper();
		String userInfoJson = objectMapper.writeValueAsString(claims);

		return Jwts.builder()
				.setSubject(userInfo.getUserid())
				.claim("userinfo", userInfoJson) // JSON 형태로 클레임 추가
				.setIssuedAt(new Date())
				.setExpiration(expiration)
				.signWith(secretKey)
				.compact();
	}

	public Jws<Claims> getClaims(String token) {
		SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessTokenSecret));
		Jws<Claims> claims = Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);

		return claims;
	}

	public long getAccessTokenExpiration() {
		return accessTokenExpiration;
	}

	public boolean validateToken(String token) {
		SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessTokenSecret));
		try {
			Jws<Claims> claimsJwts = Jwts.parserBuilder()
					.setSigningKey(secretKey)
					.build()
					.parseClaimsJws(token);

			Date expiration = claimsJwts.getBody().getExpiration();

			if (null != expiration) {
				return !expiration.before(new Date());
			}

			return true;
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

		return false;
	}
}