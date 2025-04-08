package com.ql.BlogApplication.service;
import com.ql.BlogApplication.dto.*;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.mapper.UserMapper;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {

      private final UserRepository userRepository;
      private final RoleRepository roleRepository;
      private final UserRoleRepository userRoleRepository;
      private final BCryptPasswordEncoder passwordEncoder;

      //Working properly
      public UserService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, BCryptPasswordEncoder passwordEncoder){
          this.userRepository=userRepository;
          this.roleRepository=roleRepository;
          this.userRoleRepository=userRoleRepository;
          this.passwordEncoder = passwordEncoder;
      }

      //Working properly
      public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers(){
        List<User> allUsers= userRepository.findAll();

        List<UserResponseDto> userResponseDtoList= UserMapper.toDtoList(allUsers);

        ApiResponse<List<UserResponseDto>> apiResponse= ApiResponse.<List<UserResponseDto>>success(HttpStatus.OK.value(), userResponseDtoList,"All users fetched successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

      //Working properly
      public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(Long id){

           Optional<User> user=userRepository.findById(id);

           if(user.isPresent()){
               UserResponseDto userResponseDto=UserMapper.toDto(user.get());
               ApiResponse<UserResponseDto> apiResponse= ApiResponse.<UserResponseDto>success(HttpStatus.OK.value(), userResponseDto,"Successfully find user with this id");
               return new ResponseEntity<>(apiResponse,HttpStatus.OK);
           }

           ApiResponse<UserResponseDto> apiResponse= ApiResponse.error(HttpStatus.NOT_FOUND.value(),null, "No user found");
           return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
      }

      //Working properly
      public ResponseEntity<ApiResponse<String>> deleteUserById(Long id){

             if(!userRepository.existsById(id)){
                 ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"User not found","User not found");
                 return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
             }
             userRepository.deleteById(id);
             ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"User deleted successfully","User deleted successfully");
             return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //Not working properly
      @Transactional
      public ResponseEntity<ApiResponse<String>> updateUserByID(Long id, UserUpdateRequestDto userUpdateRequestDto ){

            Optional<User> user=userRepository.findById(id);

            if(user.isEmpty()){
                ApiResponse<String> apiResponse= ApiResponse.error(HttpStatus.NOT_FOUND.value(),"No user found","No user found");
                return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
            }

            User userToUpdate=user.get();
            userToUpdate.setName(userUpdateRequestDto.getName());
            userToUpdate.setEmail(userUpdateRequestDto.getEmail());
            userToUpdate.setPassword(passwordEncoder.encode(userUpdateRequestDto.getPassword()));

            userRepository.save(userToUpdate);

            ApiResponse<String> apiResponse= ApiResponse.<String>success(HttpStatus.OK.value(), "User updated successfully","User updated successfully");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //Working properly
      public ResponseEntity<ApiResponse<String>> addUserRoleById(Long id, RoleRequestDto roleRequestDto){

          Optional<User> existingUser=userRepository.findById(id);

          if(existingUser.isEmpty()){
              ApiResponse<String> apiResponse= ApiResponse.error(HttpStatus.NOT_FOUND.value(),"No user found","No user found");
              return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
          }

          Optional<Role> optionalRole=roleRepository.findByName(roleRequestDto.getRole());

          if(optionalRole.isEmpty()){
              return new ResponseEntity<>(ApiResponse.error(
                      HttpStatus.BAD_REQUEST.value(), "Invalid role name.", "Validation error."),
                      HttpStatus.BAD_REQUEST);
          }

          Optional<UserRole> optionalUserRole=userRoleRepository.findByUserIdAndRoleId(id,optionalRole.get().getId());

          if(optionalUserRole.isPresent()){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.CONFLICT.value(),"User already has this role","User already has this role");
              return new ResponseEntity<>(apiResponse,HttpStatus.CONFLICT);
          }

          Role role=optionalRole.get();
          User user=existingUser.get();
          UserRole userRole=new UserRole();
          userRole.setRole(role);
          userRole.setUser(user);
          userRoleRepository.save(userRole);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Role assigned","Role assigned");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

}
