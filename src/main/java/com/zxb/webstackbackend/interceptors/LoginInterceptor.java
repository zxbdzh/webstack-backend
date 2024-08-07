package com.zxb.webstackbackend.interceptors;

import com.zxb.webstackbackend.utils.JwtUtil;
import com.zxb.webstackbackend.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Objects;

/**
 * @version 1.0
 * @author:没有名字的名字
 * @Date:2023/12/15
 * @Project:big-event
 */

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果是OPTIONS请求，则直接放行
        if (request.getMethod().equals(HttpMethod.OPTIONS.name()))
            return true;

        // 令牌验证
        log.info(request.getMethod());
        String token = request.getHeader("Authorization");
        log.info("token: {}", token);
        // 验证token
        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            // 把业务数据存储到ThreadLocal对象
            ThreadLocalUtil.set(claims);
            Integer role = (Integer) claims.get("role");
            log.info(String.valueOf(role));
            if (role == 1) return true;

            if (Objects.equals(request.getRequestURI(), "/user/userInfo")) return true;
            else {
                response.setStatus(402);
                return false;
            }
        } catch (Exception e) {
            log.error("error: ", e);
            // http 相应状态码为401
            response.setStatus(401);
            // 不放行
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }
}
