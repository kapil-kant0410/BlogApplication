package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.*;
import com.ql.BlogApplication.entity.Otp;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.RoleNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.repository.OtpRepository;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.*;


@Service
public class AuthService {

    Logger logger= LoggerFactory.getLogger(AuthService.class);

    @Value("${mail.otp.subject}")
    private String otpSubject;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpRepository otpRepository;
    private final JavaMailSender javaMailSender;
    private final JwtUtil jwtUtil;
    private final Random random=new Random();

      AuthService(UserRepository userRepository, RoleRepository roleRepository,JwtUtil jwtUtils,JavaMailSender javaMailSender,OtpRepository otpRepository){
              this.userRepository=userRepository;
              this.roleRepository=roleRepository;
              this.jwtUtil=jwtUtils;
              this.javaMailSender=javaMailSender;
              this.otpRepository=otpRepository;
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

        Set<String> roleIds=new HashSet<>();
        roleIds.add(role.getId());
        newUser.setRoleIds(roleIds);
        userRepository.save(newUser);

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

           Otp otpEntity=new Otp();
           otpEntity.setEmail(otpGenerationRequestDto.getEmail());
           otpEntity.setOtp(otp);
           otpEntity.setGeneratedAt(LocalDateTime.now());

           otpRepository.save(otpEntity);

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
          Optional<Otp> optionalOtp = otpRepository.findTopByEmailOrderByGeneratedAtDesc(otpValidationRequestDto.getEmail());

          if(optionalOtp.isEmpty()){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid otp","Invalid otp");
              return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
          }

          Otp otp = optionalOtp.get();

          if (otp.getGeneratedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Otp expired","Otp expired");
              return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
          }

          if (!otp.getOtp().equals(otpValidationRequestDto.getOtp())) {
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid otp","Invalid otp");
              return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
          }

          otpRepository.delete(otp);

          String token=jwtUtil.generateToken(user.getId());

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), token,"Login successfully");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      public ResponseEntity<ApiResponse<String>> logout(){

          String token= TokenContext.getToken();
          String id= jwtUtil.extractId(token);

          User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

          user.setTokenVersion(user.getTokenVersion()+1);
          userRepository.save(user);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(106),MessageCodes.messages.get(106));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }
}
