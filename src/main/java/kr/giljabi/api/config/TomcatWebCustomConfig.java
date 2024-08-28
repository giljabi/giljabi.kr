package kr.giljabi.api.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * SpringBoot는 내장 Tomcat(톰캣)을 사용하고 있으며, 이 Tomcat 의 특정 버전 이상에서 RFC 3986 규격이 적용되었다고 한다.
 * RFC 3986에는 영어 문자(a-zA-Z), 숫자(0-9), -. ~4 특수 문자 및 모든 예약 문자만 허용된다.
 * 따라서 한글을 URL 쿼리스트링으로 변환하며 생기는 특수문자가 원인이다.
 * relaxQueryChars 옵션에 허용할 문자를 추가하거나 톰캣 버전을 다운그레이드해야한다.
 * @Date: 2024.06.30
[2024-06-26 02:36:11.507464106] INFO  8704[nio-9090-exec-4] o.a.coyote.http11.Http11Processor.log : Error parsing HTTP request header
Note: further occurrences of HTTP request parsing errors will be logged at DEBUG level.
java.lang.IllegalArgumentException: Invalid character found in the request target [/?id=%25{{{11}}*{{11}}} ]. The valid characters are defined in RFC 7230 and RFC 3986
at org.apache.coyote.http11.Http11InputBuffer.parseRequestLine(Http11InputBuffer.java:494)
bitnami@ip-172-26-1-43:~/giljabi/logs$
 */
@Configuration
public class TomcatWebCustomConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers(connector -> connector.setProperty("relaxedQueryChars", "<>[\\]^`{|}"));
    }
}