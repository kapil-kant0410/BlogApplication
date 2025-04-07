package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.UserLoginRequestDto;
import com.ql.BlogApplication.dto.UserRegisterRequestDto;
import com.ql.BlogApplication.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

       private final AuthService authService;

       @PostMapping("/register")
       public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody UserRegisterRequestDto userRequestDto){
           return authService.registerUser(userRequestDto);
       }

      @PostMapping("/login")
      public ResponseEntity<ApiResponse<String>> loginUser(@RequestBody UserLoginRequestDto authRequestDto) {
             return authService.login(authRequestDto);
      }


}
