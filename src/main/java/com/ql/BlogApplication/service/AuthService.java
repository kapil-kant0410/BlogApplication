package com.ql.BlogApplication.service;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.UserLoginRequestDto;
import com.ql.BlogApplication.dto.UserRegisterRequestDto;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.repository.UserRoleRepository;
import com.ql.BlogApplication.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtUtil jwtUtils;

      AuthService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, JwtUtil jwtUtils){
              this.userRepository=userRepository;
              this.roleRepository=roleRepository;
              this.userRoleRepository=userRoleRepository;
              this.jwtUtils=jwtUtils;
      }

      public ResponseEntity<ApiResponse<String>> registerUser(UserRegisterRequestDto userRequestDto){

        if(userRepository.existsByEmail(userRequestDto.getEmail())){
            ApiResponse<String> apiResponse=  ApiResponse.<String>error(HttpStatus.CONFLICT.value(), "Email already exists.","Validation error.");
            return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
        }

        Optional<Role> optionalRole =roleRepository.findByName(userRequestDto.getRole());

        if(optionalRole.isEmpty()){
            return new ResponseEntity<>(ApiResponse.<String>error(
                    HttpStatus.BAD_REQUEST.value(), "Invalid role name.", "Validation error."),
                    HttpStatus.BAD_REQUEST);
        }

        Role role=optionalRole.get();
        User newUser = new User();
        newUser.setName(userRequestDto.getName());
        newUser.setEmail(userRequestDto.getEmail());
        newUser.setPassword(userRequestDto.getPassword());
        userRepository.save(newUser);

        UserRole userRole=new UserRole();
        userRole.setUser(newUser);
        userRole.setRole(role);

        userRoleRepository.save(userRole);

        ApiResponse<String> apiResponse= ApiResponse.<String>success(HttpStatus.CREATED.value(), jwtUtils.generateToken(newUser.getId()),"User registered successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.CREATED);
    }

      public ResponseEntity<ApiResponse<String>> login(UserLoginRequestDto authRequestDto){

            Optional<User> user=userRepository.findByEmail(authRequestDto.getEmail());

            if(user.isEmpty()){
                ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.NOT_FOUND.value(), "Invalid credentials","Invalid credentials");
                return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
            }

             String dataBasePassword=user.get().getPassword();
             String requestPassword=authRequestDto.getPassword();

             if(!Objects.equals(dataBasePassword, requestPassword)){
                 ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.UNAUTHORIZED.value(), "Password mismatched","password mismatched");
                 return new ResponseEntity<>(apiResponse,HttpStatus.UNAUTHORIZED);
             }

            String token=jwtUtils.generateToken(user.get().getId());

            ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), token,"Successfully signed in.");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }
}
