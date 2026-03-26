package com.edu.course.config;

import com.edu.common.security.context.UserContext;
import com.edu.common.security.model.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 网关传递用户信息过滤器
 * 从请求Header中读取网关解析好的用户信息，存入UserContext
 */
@Slf4j
@Component
public class UserContextFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-User-Name";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);
        String role = request.getHeader(HEADER_USER_ROLE);

        if (StringUtils.hasText(userId)) {
            try {
                LoginUser loginUser = LoginUser.builder()
                        .userId(Long.parseLong(userId))
                        .username(username)
                        .role(role)
                        .build();
                UserContext.setCurrentUser(loginUser);
            } catch (NumberFormatException e) {
                log.warn("解析用户ID失败: {}", userId);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
