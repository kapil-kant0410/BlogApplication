package com.ql.BlogApplication.service;

import com.ql.BlogApplication.config.JwtUtil;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.UserLoginRequestDto;
import com.ql.BlogApplication.dto.UserRegisterRequestDto;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.repository.UserRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtUtil jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

      AuthService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, JwtUtil jwtUtils, BCryptPasswordEncoder passwordEncoder){
              this.userRepository=userRepository;
              this.roleRepository=roleRepository;
              this.userRoleRepository=userRoleRepository;
              this.jwtUtils=jwtUtils;
              this.passwordEncoder=passwordEncoder;
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
        newUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        // newUser.setPassword(userRequestDto.getPassword());
        userRepository.save(newUser);

        UserRole userRole=new UserRole();
        userRole.setUser(newUser);
        userRole.setRole(role);

        userRoleRepository.save(userRole);

        ApiResponse<String> apiResponse= ApiResponse.<String>success(HttpStatus.CREATED.value(), jwtUtils.generateToken(userRequestDto.getEmail()),"User registered successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.CREATED);
    }

      public ResponseEntity<ApiResponse<String>> login(UserLoginRequestDto authRequestDto){

            Optional<User> user=userRepository.findByEmail(authRequestDto.getEmail());

            if(user.isEmpty()){
                ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.NOT_FOUND.value(), "Invalid credentials","Invalid credentials");
                return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
            }

            if(!passwordEncoder.matches(authRequestDto.getPassword(),user.get().getPassword())){
                ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials","Invalid credentials");
                return new ResponseEntity<>(apiResponse,HttpStatus.UNAUTHORIZED);
            }

            String token=jwtUtils.generateToken(authRequestDto.getEmail());

            ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), token,"Successfully signed in.");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }
}
