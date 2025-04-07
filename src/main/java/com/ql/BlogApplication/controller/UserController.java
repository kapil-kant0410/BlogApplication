package com.ql.BlogApplication.controller;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.RoleRequestDto;
import com.ql.BlogApplication.dto.UserRegisterRequestDto;
import com.ql.BlogApplication.dto.UserResponseDto;
import com.ql.BlogApplication.entity.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ql.BlogApplication.service.UserService;
import java.util.List;


@RestController
@RequestMapping("/api/user")
@AllArgsConstructor

public class UserController {

     private static final Logger logger=  LoggerFactory.getLogger(UserController.class);
     private final UserService userService;

     @GetMapping("/all")
     public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers(){
          logger.info("Fetching all users.");
          return userService.getAllUsers();
     }

     @GetMapping("/{id}")
     public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id){
         logger.info("Fetching user with ID: {}", id);
         return userService.getUserById(id);
     }

     @DeleteMapping("/{id}")
     public ResponseEntity<ApiResponse<String>> deleteUserById(@PathVariable Long id){
        logger.info("Deleting user with ID: {}", id);
        ResponseEntity<ApiResponse<String>> response= userService.deleteUserById(id);
        logger.info("User with ID {} deleted successfully", id);
        return response;
    }

     @PutMapping("/{id}")
     public ResponseEntity<ApiResponse<String>> updateUserByID(@PathVariable Long id,@Valid @RequestBody UserRegisterRequestDto userRequestDto){
         logger.info("Updating user with ID: {} | Email: {} | Name: {}", id, userRequestDto.getEmail(), userRequestDto.getName());
         return userService.updateUserByID(id,userRequestDto);
     }

     @PostMapping("/addRole/{id}")
     public ResponseEntity<ApiResponse<String>> addUserRoleById(@PathVariable Long id, @Valid @RequestBody RoleRequestDto roleRequestDto){
         return  userService.addUserRoleById(id,roleRequestDto);
     }

}
