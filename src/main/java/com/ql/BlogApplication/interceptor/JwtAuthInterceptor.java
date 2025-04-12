package com.ql.BlogApplication.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Transactional
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    private final JwtUtil jwtUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authHeader=request.getHeader("Authorization");

        if(authHeader!=null&&authHeader.startsWith("Bearer ")){
            String token=authHeader.substring(7);
            TokenContext.setToken(token);
            String id=jwtUtil.extractId(token);
            logger.info("token is {} and id inside is  {}",token,id);
            if(jwtUtil.validateToken(token)){
                 return true;
            }
        }

        ApiResponse<String> apiResponse=ApiResponse.error(401,"Invalid or missing token","Invalid token");
        ObjectMapper objectMapper=new ObjectMapper();
        String json= objectMapper.writeValueAsString(apiResponse);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(json);
        response.getWriter().flush();
        return false;
    }

    public void afterCompletion(HttpServletRequest request,HttpServletResponse response,Object handler){
         TokenContext.clearToken();
    }

}
