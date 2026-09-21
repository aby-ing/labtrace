package com.qust.lab.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qust.lab.common.result.Result;
import com.qust.lab.mapper.UserMapper;
import com.qust.lab.pojo.entity.User;
import com.qust.lab.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String TOKEN_BLACKLIST_PREFIX =
            "jwt:blacklist:";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;

    public JwtInterceptor(
            JwtUtil jwtUtil,
            ObjectMapper objectMapper,
            StringRedisTemplate stringRedisTemplate,
            UserMapper userMapper
    ) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        // 浏览器跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头中获取 Token
        String authorization =
                request.getHeader("Authorization");

        // 请求头格式必须是：Bearer Token
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "请先登录");
            return false;
        }

        String token = authorization.substring(7).trim();

        if (token.isEmpty()) {
            writeUnauthorized(response, "Token 不能为空");
            return false;
        }

        try {
            Boolean tokenDisabled = stringRedisTemplate.hasKey(
                    TOKEN_BLACKLIST_PREFIX + token
            );

            if (Boolean.TRUE.equals(tokenDisabled)) {
                writeUnauthorized(response, "登录已退出，请重新登录");
                return false;
            }

            // 解析 Token，签名错误或过期都会抛异常
            Long userId = jwtUtil.getUserId(token);

            User user = userMapper.selectById(userId);

            if (user == null) {
                writeUnauthorized(response, "用户不存在");
                return false;
            }

            if (!Integer.valueOf(1).equals(user.getStatus())) {
                writeUnauthorized(response, "用户已被禁用");
                return false;
            }

            // 把当前用户 ID 放入请求对象
            // 后面的 Controller 或 Service 可以取出来
            request.setAttribute("userId", userId);
            request.setAttribute("role", user.getRole());

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorized(response, "登录已失效，请重新登录");
            return false;
        } catch (RedisConnectionFailureException e) {
            writeUnauthorized(response, "认证服务暂时不可用");
            return false;
        }
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String message
    ) throws Exception {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Result<Void> result = Result.error(message);

        response.getWriter().write(
                objectMapper.writeValueAsString(result)
        );
    }
}
