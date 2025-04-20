package com.ql.BlogApplication.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class AuthorInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    AuthorInterceptor(JwtUtil jwtUtil, UserRepository userRepository, RoleRepository roleRepository){
        this.jwtUtil=jwtUtil;
        this.userRepository=userRepository;
        this.roleRepository=roleRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,Object handler) throws Exception{

        String authHeader=httpServletRequest.getHeader("Authorization");

        if(authHeader!=null&&authHeader.startsWith("Bearer ")){
            String token=authHeader.substring(7);
            if(jwtUtil.validateToken((token))){
                String id= jwtUtil.extractId(token);
                User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
                Boolean isAuthor=roleRepository.findAllById(user.getRoleIds()).stream().anyMatch((role)->{
                    return "author".equals(role.getName());
                });
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
