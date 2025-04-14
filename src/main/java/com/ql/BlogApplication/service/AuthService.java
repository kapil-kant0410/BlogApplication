package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.*;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.exception.RoleNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.repository.UserRoleRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;


@Service
public class AuthService {

    Logger logger= LoggerFactory.getLogger(AuthService.class);

    @Value("${mail.otp.subject}")
    private String otpSubject;


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JavaMailSender javaMailSender;
    private final JwtUtil jwtUtil;
    private final Random random=new Random();
    private  Map<String,String> otpStore=new HashMap<>();
    private  Map<String,Long> otpExpiry=new HashMap<>();

      AuthService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, JwtUtil jwtUtils,JavaMailSender javaMailSender){
              this.userRepository=userRepository;
              this.roleRepository=roleRepository;
              this.userRoleRepository=userRoleRepository;
              this.jwtUtil=jwtUtils;
              this.javaMailSender=javaMailSender;
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

      public ResponseEntity<ApiResponse<String>> loginByPassword(UserLoginRequestDto authRequestDto){

            User user=userRepository.findByEmail(authRequestDto.getEmail()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

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

      public ResponseEntity<ApiResponse<String>> generateOtp(OtpGenerationRequestDto otpGenerationRequestDto){

          userRepository.findByEmail(otpGenerationRequestDto.getEmail()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

          String otp=String.valueOf(random.nextInt(900000)+100000);
          otpStore.put(otpGenerationRequestDto.getEmail(),otp);
          otpExpiry.put(otpGenerationRequestDto.getEmail(),System.currentTimeMillis()+(5*60*1000));

          SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
          simpleMailMessage.setTo(otpGenerationRequestDto.getEmail());
          simpleMailMessage.setSubject(otpSubject);
          simpleMailMessage.setText("Your OTP is: " + otp+"\nIt will expire in 5 minutes.");

          javaMailSender.send(simpleMailMessage);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), "Otp send successfully","Otp send successfully");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      public ResponseEntity<ApiResponse<String>> validateOtp(OtpValidationRequestDto otpValidationRequestDto){

          User user=userRepository.findByEmail(otpValidationRequestDto.getEmail()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
          String validOtp=otpStore.get(otpValidationRequestDto.getEmail());
          Long expiryTime=otpExpiry.get(otpValidationRequestDto.getEmail());

          if(expiryTime<System.currentTimeMillis()){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Otp expired","Otp expired");
              return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
          }

          if(validOtp!=null  && validOtp.equals(otpValidationRequestDto.getOtp())){
              String token=jwtUtil.generateToken(user.getId());
              otpStore.remove(otpValidationRequestDto.getEmail());
              otpExpiry.remove(otpValidationRequestDto.getEmail());
              ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), token,MessageCodes.messages.get(102));
              return new ResponseEntity<>(apiResponse,HttpStatus.OK);
          }

          ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid otp","Invalid otp");
          return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
      }

      public ResponseEntity<ApiResponse<String>> logout(){

          String token= TokenContext.getToken();
          Long id= Long.parseLong(jwtUtil.extractId(token));

          User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

          user.setTokenVersion(user.getTokenVersion()+1);
          userRepository.save(user);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(106),MessageCodes.messages.get(106));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }
}
