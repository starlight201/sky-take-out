package com.sky.interceptor;

import com.sky.properties.AgentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AgentInternalInterceptor implements HandlerInterceptor {

    private static final String TOKEN_HEADER = "X-Agent-Token";

    @Autowired
    private AgentProperties agentProperties;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String configuredToken = agentProperties.getInternalToken();
        if (!StringUtils.hasText(configuredToken)) {
            return true;
        }
        String requestToken = request.getHeader(TOKEN_HEADER);
        if (configuredToken.equals(requestToken)) {
            return true;
        }
        response.setStatus(403);
        return false;
    }
}
