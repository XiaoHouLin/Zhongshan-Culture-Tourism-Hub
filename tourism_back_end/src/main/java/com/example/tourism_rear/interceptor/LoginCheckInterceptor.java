package com.example.tourism_rear.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.pack.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final String EXPIRED_LOGIN_MESSAGE = "登录信息过期，请重新登录";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String url = request.getRequestURI();
        log.info("请求的url：{}", url);

        if (url.contains("login")
                || url.contains("register")
                || url.contains("indexView")
                || url.contains("isUnique")
                || url.contains("password-reset")
                || url.startsWith("/chat")) {
            return true;
        }

        if (HttpMethod.GET.matches(request.getMethod()) && url.startsWith("/cultural-products")) {
            return true;
        }

        String jwt = resolveToken(request);
        if (!StringUtils.hasText(jwt)) {
            writeUnauthorizedResponse(response);
            return false;
        }

        try {
            Jwt.checkToken(jwt);
        } catch (Exception e) {
            log.warn("token解析失败: {}", e.getMessage());
            writeUnauthorizedResponse(response);
            return false;
        }

        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (!StringUtils.hasText(jwt)) {
            jwt = request.getHeader("authorization");
        }
        if (StringUtils.hasText(jwt) && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        if (!StringUtils.hasText(jwt)) {
            jwt = request.getHeader("token");
        }
        return jwt;
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws Exception {
        Result error = Result.error(EXPIRED_LOGIN_MESSAGE);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(JSONObject.toJSONString(error));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // no-op
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // no-op
    }
}
