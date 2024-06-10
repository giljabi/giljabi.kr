package kr.giljabi.api.utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class TokenResponse {

	private String tokenType;
	private String accessToken;
	private String expiresIn;

	@Builder
	public TokenResponse(String tokenType,
                         String accessToken,
                         String expiresIn) {
		this.tokenType = tokenType;
		this.accessToken = accessToken;
		this.expiresIn = expiresIn;
	}

}