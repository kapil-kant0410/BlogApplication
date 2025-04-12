package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.UserLoginRequestDto;
import com.ql.BlogApplication.dto.UserRegisterRequestDto;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.exception.RoleNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.interceptor.AuthorInterceptor;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.repository.UserRoleRepository;
import com.ql.BlogApplication.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Objects;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthorInterceptor authorInterceptor;
    private final HttpServletRequest httpServletRequest;
    private final JwtUtil jwtUtil;

      AuthService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, JwtUtil jwtUtils,HttpServletRequest httpServletRequest,AuthorInterceptor authorInterceptor){
              this.userRepository=userRepository;
              this.roleRepository=roleRepository;
              this.userRoleRepository=userRoleRepository;
              this.jwtUtil=jwtUtils;
              this.httpServletRequest=httpServletRequest;
              this.authorInterceptor=authorInterceptor;
      }

      public ResponseEntity<ApiResponse<String>> registerUser(UserRegisterRequestDto userRequestDto){

        if(userRepository.existsByEmail(userRequestDto.getEmail())){
            ApiResponse<String> apiResponse=  ApiResponse.<String>error(HttpStatus.CONFLICT.value(), "Email already exists.","Validation error.");
            return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
        }

        Role role=roleRepository.findByName(userRequestDto.getRole()).orElseThrow(()->new RoleNotFoundException(MessageCodes.messages.get(107)));

        User newUser = new User();
        newUser.setName(userRequestDto.getName());
        newUser.setEmail(userRequestDto.getEmail());
        newUser.setPassword(userRequestDto.getPassword());
        userRepository.save(newUser);

        UserRole userRole=new UserRole();
        userRole.setUser(newUser);
        userRole.setRole(role);

        userRoleRepository.save(userRole);

        ApiResponse<String> apiResponse= ApiResponse.<String>success(HttpStatus.CREATED.value(), jwtUtil.generateToken(newUser.getId()),MessageCodes.messages.get(101));
        return new ResponseEntity<>(apiResponse,HttpStatus.CREATED);
    }

      public ResponseEntity<ApiResponse<String>> login(UserLoginRequestDto authRequestDto){

            User user=userRepository.findByEmail(authRequestDto.getEmail()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(106)));

             String dataBasePassword=user.getPassword();
             String requestPassword=authRequestDto.getPassword();

             if(!Objects.equals(dataBasePassword, requestPassword)){
                 ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.UNAUTHORIZED.value(), "Password mismatched","password mismatched");
                 return new ResponseEntity<>(apiResponse,HttpStatus.UNAUTHORIZED);
             }

            String token=jwtUtil.generateToken(user.getId());

            ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), token,MessageCodes.messages.get(102));
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      public ResponseEntity<ApiResponse<String>> logout(){

          String token= authorInterceptor.getToken(httpServletRequest);
          Long id= Long.parseLong(jwtUtil.extractId(token));

          User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(106)));

          user.setTokenVersion(user.getTokenVersion()+1);
          userRepository.save(user);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"User logged out successfully","User logged out successfully");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }
}
