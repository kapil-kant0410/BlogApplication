package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.*;
import com.ql.BlogApplication.entity.Otp;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.exception.RoleNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.mapper.UserMapper;
import com.ql.BlogApplication.repository.OtpRepository;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.repository.UserRoleRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class AuthService {

    Logger logger= LoggerFactory.getLogger(AuthService.class);

    @Value("${otp.subject}")
    private String otpSubject;

    @Value("${otp.message}")
    private String otpMessage;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final OtpRepository otpRepository;
    private final JavaMailSender javaMailSender;
    private final JwtUtil jwtUtil;
    private final Random random=new Random();

      AuthService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, JwtUtil jwtUtils,JavaMailSender javaMailSender,OtpRepository otpRepository){
              this.userRepository=userRepository;
              this.roleRepository=roleRepository;
              this.userRoleRepository=userRoleRepository;
              this.jwtUtil=jwtUtils;
              this.javaMailSender=javaMailSender;
              this.otpRepository=otpRepository;
      }

      @Transactional
      public ResponseEntity<ApiResponse<Map<String,String>>> registerUser(UserRegisterRequestDto userRequestDto){

        if(userRepository.existsByEmail(userRequestDto.getEmail())){
            ApiResponse<Map<String,String>> apiResponse=  ApiResponse.error(HttpStatus.CONFLICT.value(), null,"Email already exists.");
            return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
        }

        Role role=roleRepository.findByName(userRequestDto.getRole()).orElseThrow(()->new RoleNotFoundException(MessageCodes.messages.get(107)));

        User newUser = new User();
        newUser.setName(userRequestDto.getName());
        newUser.setEmail(userRequestDto.getEmail());
        newUser.setPassword(userRequestDto.getPassword());

        UserRole userRole=new UserRole();
        userRole.setUser(newUser);
        userRole.setRole(role);

        userRepository.save(newUser);
        userRoleRepository.save(userRole);

        Map<String,String> data=new HashMap<>();
        data.put("user_name",newUser.getName());
        data.put("user_email",newUser.getEmail());

        ApiResponse<Map<String,String>> apiResponse=ApiResponse.success(HttpStatus.CREATED.value(), data,MessageCodes.messages.get(101));
        return new ResponseEntity<>(apiResponse,HttpStatus.CREATED);
    }

      public ResponseEntity<ApiResponse<Map<String,String>>> loginByPassword(UserLoginRequestDto authRequestDto){

            User user=userRepository.findByEmail(authRequestDto.getEmail()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

             String dataBasePassword=user.getPassword();
             String requestPassword=authRequestDto.getPassword();

             if(!Objects.equals(dataBasePassword, requestPassword)){
                 ApiResponse<Map<String,String>> apiResponse=ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), null,"password mismatched");
                 return new ResponseEntity<>(apiResponse,HttpStatus.UNAUTHORIZED);
             }

            String accessToken=jwtUtil.generateAccessToken(user.getId());
            String refreshToken=jwtUtil.generateRefreshToken(user.getId());

            Map<String,String> data=new HashMap<>();
            data.put("User_name",user.getName());
            data.put("User_email",user.getEmail());
            data.put("access_token",accessToken);
            data.put("refresh_token",refreshToken);
            ApiResponse<Map<String,String>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), data,MessageCodes.messages.get(102));
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      public ResponseEntity<ApiResponse<Map<String,String>>> generateOtp(OtpGenerationRequestDto otpGenerationRequestDto){

          try{
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
              simpleMailMessage.setText(String.format(otpMessage,otp));

              javaMailSender.send(simpleMailMessage);


              ApiResponse<Map<String,String>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), Collections.emptyMap(),"Otp send successfully");
              return new ResponseEntity<>(apiResponse,HttpStatus.OK);
          }catch(MailException e){
              ApiResponse<Map<String, String>> apiResponse = ApiResponse.error(
                      HttpStatus.INTERNAL_SERVER_ERROR.value(),
                       null,
                      "An unexpected mail exception during otp generation"
              );
              return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
          }

      }

      public ResponseEntity<ApiResponse<Map<String,String>>> validateOtp(OtpValidationRequestDto otpValidationRequestDto){

          User user=userRepository.findByEmail(otpValidationRequestDto.getEmail()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
          Optional<Otp> optionalOtp = otpRepository.findTopByEmailOrderByGeneratedAtDesc(otpValidationRequestDto.getEmail());

          if(optionalOtp.isEmpty()){
              ApiResponse<Map<String,String>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), null,"Otp not found");
              return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
          }

          Otp otp = optionalOtp.get();

          if (otp.getGeneratedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
              ApiResponse<Map<String,String>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), null,"Otp expired");
              return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
          }

          if (!otp.getOtp().equals(otpValidationRequestDto.getOtp())) {
              ApiResponse<Map<String,String>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), null,"Invalid otp");
              return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
          }

          otpRepository.delete(otp);
          String accessToken=jwtUtil.generateAccessToken(user.getId());
          String refreshToken=jwtUtil.generateRefreshToken(user.getId());
          Map<String,String> data=new HashMap<>();
          data.put("access_token",accessToken);
          data.put("refresh_token",refreshToken);
          data.put("user_name",user.getName());
          data.put("email",user.getEmail());

          ApiResponse<Map<String,String>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), data,"Login successfully");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      public ResponseEntity<ApiResponse<Map<String,String>>> logout(){

          String token= TokenContext.getToken();
          Long id= Long.parseLong(jwtUtil.extractId(token));

          User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

          user.setTokenVersion(user.getTokenVersion()+1);
          userRepository.save(user);

          ApiResponse<Map<String,String>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),Collections.emptyMap(),MessageCodes.messages.get(106));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }
}
