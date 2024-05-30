package kr.giljabi.api.config;

/**
 * Jackson 설정을 통해 LocalDateTime의 JSON 직렬화/역직렬화를 처리할 수 있습니다.
 *
 * 위의 설정을 통해 Java 17에서 java.sql.Timestamp와 java.time.LocalDateTime을 모두 사용할 수 있으며, 각 타입에 맞는 설정을 적용할 수 있습니다.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        //objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}