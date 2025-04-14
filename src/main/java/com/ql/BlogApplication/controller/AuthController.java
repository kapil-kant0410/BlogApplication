package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.*;
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
       public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody UserRegisterRequestDto userRegisterRequestDto){
           return authService.registerUser(userRegisterRequestDto);
       }

      @PostMapping("/login")
      public ResponseEntity<ApiResponse<String>> loginByPassword(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
             return authService.loginByPassword(userLoginRequestDto);
      }

      @PostMapping("/generate-otp")
      public ResponseEntity<ApiResponse<String>> generateOtp(@Valid @RequestBody OtpGenerationRequestDto otpGenerationRequestDto){
         return authService.generateOtp(otpGenerationRequestDto);
      }

      @PostMapping("/validate-otp")
      public ResponseEntity<ApiResponse<String>> validateOtp(@Valid @RequestBody OtpValidationRequestDto userValidateOtpLoginRequestDto){
        return authService.validateOtp(userValidateOtpLoginRequestDto);
      }

      @PostMapping("/logout")
      public ResponseEntity<ApiResponse<String>> logout(){
           return authService.logout();
      }


}
