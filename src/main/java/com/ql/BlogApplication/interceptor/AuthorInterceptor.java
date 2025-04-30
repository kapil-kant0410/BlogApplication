package com.ql.BlogApplication.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.repository.UserRepository;
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
public class AuthorInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthorInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    AuthorInterceptor(JwtUtil jwtUtil,UserRepository userRepository){
        this.jwtUtil=jwtUtil;
        this.userRepository=userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,Object handler) throws Exception{

        String authHeader=httpServletRequest.getHeader("Authorization");

        if(authHeader!=null&&authHeader.startsWith("Bearer ")){
            String token=authHeader.substring(7);
            if(jwtUtil.validateToken((token))){
                Long id= Long.parseLong(jwtUtil.extractId(token));
                User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
                Boolean isAuthor= user.getUserRoles().stream().map(userRole -> userRole.getRole().getName()).anyMatch("author"::equals);
                TokenContext.setToken(token);
                if(Boolean.TRUE.equals(isAuthor)) return true;
            }
        }

        ApiResponse<String> apiResponse=ApiResponse.error(401,"Unauthorized: Only authors can access this resource.","Unauthorized");
        ObjectMapper objectMapper=new ObjectMapper();
        String json= objectMapper.writeValueAsString(apiResponse);
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.getWriter().write(json);
        return false;
    }

}
