package kr.giljabi.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * request, response 로그
 * POST데이터를 로깅하기 위해 사용
 *   CachingBodyFilter
 *   CachingFilterConfig
 *   CachingPostHttpServletRequest
 *
 * @author eahn.park@gmail.com
 * @date 2022.03.29
 */
@Aspect
@Component
@Slf4j
public class LoggerAspect {

    @Pointcut("execution(* *.*(..)) && (" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping))")
    public void requestMappingMethods() {
    }

    @Around("requestMappingMethods()")
    public Object methodLogger(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes())
                .getRequest();

        String controllerName = pjp.getSignature().getDeclaringType().getSimpleName();
        String methodName = pjp.getSignature().getName();
        String remoteAddr = request.getHeader("X-Forwarded-For"); //nginx 사용시 remoteAddr
        String mappingType = getMappingType(pjp);

        long startAt = System.currentTimeMillis();
        List<JSONObject> paramList;
        if(mappingType.compareTo("POST") == 0)
            paramList = convertArgsToJSONObjectList(pjp.getArgs());
        else if(mappingType.compareTo("GET") == 0)
            paramList = getParams(request);
        else
            paramList = new ArrayList<>();

        log.info("===========================================");
        log.info("IP:{} {} {}", remoteAddr, mappingType, request.getRequestURI());
        for(JSONObject map : paramList) {
            log.info("param:{}", getLogString(map.toString()));
        }

        Object result = pjp.proceed();
        ObjectMapper objectMapper = new ObjectMapper();
        String resultJson = objectMapper.writeValueAsString(result);
        log.info("response json:{}", getLogString(resultJson));

        long endAt = System.currentTimeMillis();
        log.info("Time Required : {}ms", endAt - startAt);

        return result;
    }

    private String getLogString(String message) {
        return message.length() > 100
                ? message.substring(0, 200) + "..."
                : message;
    }

    public List<JSONObject> convertArgsToJSONObjectList(Object[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<JSONObject> jsonObjectList = new ArrayList<>();

        for (Object arg : args) {
            if (arg instanceof JSONObject) {
                jsonObjectList.add((JSONObject) arg);
            } else {
                try {
                    String json = objectMapper.writeValueAsString(arg);
                    JSONObject jsonObject = new JSONObject(json);
                    jsonObjectList.add(jsonObject);
                } catch (Exception e) {
                    jsonObjectList.add(new JSONObject());
                }
            }
        }
        return jsonObjectList;
    }

    /**
     * request 에 담긴 정보를 JSONObject 형태로 반환한다.
     *
     * @return JSONObject
     */
    private static List<JSONObject> getParams(HttpServletRequest request) {
        List<JSONObject> jsonObjectList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();

        Enumeration<String> params = request.getParameterNames();

        while (params.hasMoreElements()) {
            String param = params.nextElement();
            jsonObject.put(param, request.getParameter(param));
        }
        jsonObjectList.add(jsonObject);
        return jsonObjectList;
/*        return jsonObject.toString()
                .replace("\\", "")
                .replace("\"[", "[")
                .replace("]\"", "]");*/
    }

    public String getMappingType(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        if (method.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        }

        return "UNKNOWN";  // Default fall-back
    }
}

