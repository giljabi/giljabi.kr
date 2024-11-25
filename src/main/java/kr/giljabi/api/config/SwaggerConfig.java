package kr.giljabi.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Profile({"local"})
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
        .title("API 상세소개 및 사용법 등")
        .version("1.0")
        .description("API 소개");

        OpenAPI openApi = new OpenAPI()
        .info(info);

        return openApi;
    }
}

