package com.ql.BlogApplication.service;
import com.ql.BlogApplication.dto.*;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.exception.RoleNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.interceptor.AuthorInterceptor;
import com.ql.BlogApplication.mapper.UserMapper;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.repository.UserRoleRepository;
import com.ql.BlogApplication.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {

      private final UserRepository userRepository;
      private final RoleRepository roleRepository;
      private final UserRoleRepository userRoleRepository;
      private final JwtUtil jwtUtil;
      private final AuthorInterceptor authorInterceptor;
      private final HttpServletRequest httpServletRequest;

      //Working properly
      public UserService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository,JwtUtil jwtUtil,AuthorInterceptor authorInterceptor,HttpServletRequest httpServletRequest){
          this.userRepository=userRepository;
          this.roleRepository=roleRepository;
          this.userRoleRepository=userRoleRepository;
          this.jwtUtil=jwtUtil;
          this.authorInterceptor=authorInterceptor;
          this.httpServletRequest=httpServletRequest;
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
          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found"));
          UserResponseDto userResponseDto=UserMapper.toDto(user);
          ApiResponse<UserResponseDto> apiResponse= ApiResponse.<UserResponseDto>success(HttpStatus.OK.value(), userResponseDto,"Successfully find user with this id");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //Working properly
      public ResponseEntity<ApiResponse<String>> deleteUserById(){

            String token= authorInterceptor.getToken(httpServletRequest);
            Long id= Long.parseLong(jwtUtil.extractId(token));

            boolean isUserExists=userRepository.existsById(id);
            if(!isUserExists){
              throw new UserNotFoundException("User not found");
            }
             userRepository.deleteById(id);
             ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"User deleted successfully","User deleted successfully");
             return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //Not working properly
      @Transactional
      public ResponseEntity<ApiResponse<String>> updateUserByID( UserUpdateRequestDto userUpdateRequestDto ){

            String token= authorInterceptor.getToken(httpServletRequest);
            Long id= Long.parseLong(jwtUtil.extractId(token));

            User userToUpdate=userRepository.findById(id).orElseThrow(()->new UserNotFoundException("User not found"));

            boolean isUpdated=false;

            if(!Objects.equals(userToUpdate.getName(), userUpdateRequestDto.getName())){
                userToUpdate.setName(userUpdateRequestDto.getName());
                isUpdated=true;
            }

           if(!Objects.equals(userToUpdate.getEmail(), userUpdateRequestDto.getEmail())){
               userToUpdate.setEmail(userUpdateRequestDto.getEmail());
              isUpdated=true;
           }

          if(!Objects.equals(userToUpdate.getPassword(), userUpdateRequestDto.getPassword())){
              userToUpdate.setPassword(userUpdateRequestDto.getPassword());
              isUpdated=true;
          }

          if(!isUpdated){
              ApiResponse<String> apiResponse= ApiResponse.<String>success(HttpStatus.OK.value(), "No changes made","No changes made");
              return new ResponseEntity<>(apiResponse,HttpStatus.OK);
          }

          userRepository.save(userToUpdate);

          ApiResponse<String> apiResponse= ApiResponse.<String>success(HttpStatus.OK.value(), "User updated successfully","User updated successfully");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //Working properly
      public ResponseEntity<ApiResponse<String>> addUserRoleById(RoleRequestDto roleRequestDto){

          String token= authorInterceptor.getToken(httpServletRequest);
          Long id= Long.parseLong(jwtUtil.extractId(token));

          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found"));
          Role role=roleRepository.findByName(roleRequestDto.getRole()).orElseThrow(()->new RoleNotFoundException("Role not found"));

          Optional<UserRole> optionalUserRole=userRoleRepository.findByUserIdAndRoleId(id,role.getId());

          if(optionalUserRole.isPresent()){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.CONFLICT.value(),"User already has this role","User already has this role");
              return new ResponseEntity<>(apiResponse,HttpStatus.CONFLICT);
          }

          UserRole userRole=new UserRole();
          userRole.setRole(role);
          userRole.setUser(user);
          userRoleRepository.save(userRole);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Role assigned","Role assigned");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

}
