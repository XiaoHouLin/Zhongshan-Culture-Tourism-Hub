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

    @Override//目标资源方法运行前运行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理 OPTIONS 请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        //获取请求url
        String url = request.getRequestURI();
        log.info("请求的url：{}", url);
        if (url.contains("login")
                || url.contains("register")
                || url.contains("indexView")
                || url.contains("isUnique")
                || url.contains("password-reset")) {
            //放行
            return true;
        }
        if (HttpMethod.GET.matches(request.getMethod()) && url.startsWith("/cultural-products")) {
            return true;
        }
        String jwt = request.getHeader("authorization");
        if (jwt != null && jwt.contains("Bearer ")) {
            jwt = jwt.substring(7);
        }
        if (!StringUtils.hasLength(jwt)) {
            log.info("请求头token为空");
            Result error = Result.error("Not Login");
            String notLogin = JSONObject.toJSONString(error);
            response.getWriter().write(notLogin);
            response.setStatus(401);
            return false;
        }
        try {
            //校验jwt
            Jwt.checkToken(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("解析令牌失败");
            Result error = Result.error("尚未登录，请登录");
            String notLogin = JSONObject.toJSONString(error);
            response.getWriter().write(notLogin);
            //校验失败，返回401状态码
            response.setStatus(401);
            return false;
        }
        log.info("令牌合法，放行");
        return true;
    }

    @Override//目标资源方法运行后运行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("postHandle...");
    }

    @Override//请求处理完成后运行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("afterCompletion...");
    }
}
