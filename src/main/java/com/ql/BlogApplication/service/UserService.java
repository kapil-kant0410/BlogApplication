package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.*;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.exception.RoleNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.mapper.UserMapper;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.repository.UserRoleRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {

      Logger logger= LoggerFactory.getLogger(UserService.class);

      private final UserRepository userRepository;
      private final RoleRepository roleRepository;
      private final UserRoleRepository userRoleRepository;
      private final JwtUtil jwtUtil;

      //Working properly
      public UserService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository,JwtUtil jwtUtil){
          this.userRepository=userRepository;
          this.roleRepository=roleRepository;
          this.userRoleRepository=userRoleRepository;
          this.jwtUtil=jwtUtil;
      }

      //Working properly
      public ResponseEntity<ApiResponse<Map<String, List<UserResponseDto>>>> getAllUsers() {
          List<User> allUsers = userRepository.findAll();
          List<UserResponseDto> userResponseDtoList = UserMapper.toDtoList(allUsers);
          Map<String, List<UserResponseDto>> data = new HashMap<>();
          data.put("All Users", userResponseDtoList);

          ApiResponse<Map<String, List<UserResponseDto>>> apiResponse =
                  ApiResponse.success(HttpStatus.OK.value(), data, "All users fetched successfully");

          return new ResponseEntity<>(apiResponse, HttpStatus.OK);
      }

      //Working properly
      public ResponseEntity<ApiResponse<Map<String,UserResponseDto>>> getUserById(Long id){
          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
          UserResponseDto userResponseDto=UserMapper.toDto(user);
          Map<String,UserResponseDto> data=new HashMap<>();
          data.put("User",userResponseDto);
          ApiResponse<Map<String,UserResponseDto>> apiResponse= ApiResponse.<Map<String,UserResponseDto>>success(HttpStatus.OK.value(),data,MessageCodes.messages.get(105));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //working properly
      @Transactional
      public ResponseEntity<ApiResponse<Map<String,UserResponseDto>>> updateUserByID( UserUpdateRequestDto userUpdateRequestDto ){

            String token= TokenContext.getToken();
            Long id= Long.parseLong(jwtUtil.extractId(token));

            User userToUpdate=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

            boolean isUpdated=false;

            if(!Objects.equals(userToUpdate.getName(), userUpdateRequestDto.getName())){
                userToUpdate.setName(userUpdateRequestDto.getName());
                isUpdated=true;
            }

           if(!Objects.equals(userToUpdate.getPassword(), userUpdateRequestDto.getPassword())){
              userToUpdate.setPassword(userUpdateRequestDto.getPassword());
              isUpdated=true;
            }

            if(!isUpdated){
              ApiResponse<Map<String,UserResponseDto>> apiResponse= ApiResponse.<Map<String,UserResponseDto>>error(HttpStatus.BAD_REQUEST.value(),null,"No changes made");
              return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
            }

            userRepository.save(userToUpdate);

            UserResponseDto userDto=UserMapper.toDto(userToUpdate);

            Map<String,UserResponseDto> data=new HashMap<>();
            data.put("Updated user",userDto);

           ApiResponse<Map<String,UserResponseDto>> apiResponse= ApiResponse.<Map<String,UserResponseDto>>success(HttpStatus.OK.value(), data,MessageCodes.messages.get(103));
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //Working properly
      public ResponseEntity<ApiResponse<Map<String,UserResponseDto>>> deleteUserById(){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        User user=userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        userRepository.deleteById(userId);
        UserResponseDto userResponseDto=UserMapper.toDto(user);

        Map<String,UserResponseDto> data=new HashMap<>();
        data.put("Deleted user",userResponseDto);

        ApiResponse<Map<String,UserResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,MessageCodes.messages.get(104));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

      //Working properly
      public ResponseEntity<ApiResponse<Map<String,UserResponseDto>>> addUserRoleById(RoleRequestDto roleRequestDto){

          String token= TokenContext.getToken();
          Long id= Long.parseLong(jwtUtil.extractId(token));

          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
          Role role=roleRepository.findByName(roleRequestDto.getRole()).orElseThrow(()->new RoleNotFoundException(MessageCodes.messages.get(211)));

          Optional<UserRole> optionalUserRole=userRoleRepository.findByUserIdAndRoleId(id,role.getId());

          if(optionalUserRole.isPresent()){
              ApiResponse<Map<String,UserResponseDto>> apiResponse=ApiResponse.error(HttpStatus.CONFLICT.value(),null,"User already has this role");
              return new ResponseEntity<>(apiResponse,HttpStatus.CONFLICT);
          }

          UserRole userRole=new UserRole();
          userRole.setRole(role);
          userRole.setUser(user);
          userRoleRepository.save(userRole);

          UserResponseDto userResponseDto= UserMapper.toDto(user);
          Map<String,UserResponseDto> data=new HashMap<>();
          data.put("updatedUser",userResponseDto);


          ApiResponse<Map<String,UserResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,MessageCodes.messages.get(111));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

}
