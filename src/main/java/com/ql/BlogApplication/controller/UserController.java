package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.*;
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
     public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable String id){
         logger.info("Fetching user with ID: {}", id);
         return userService.getUserById(id);
     }

     @DeleteMapping("/delete")
     public ResponseEntity<ApiResponse<String>> deleteUserById(){
        logger.info("Deleting user");
        ResponseEntity<ApiResponse<String>> response= userService.deleteUserById();
        logger.info("User with ID deleted");
        return response;
    }

     @PutMapping("/update")
     public ResponseEntity<ApiResponse<String>> updateUserByID(@Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto){
         logger.info("Updating user with Name: {}", userUpdateRequestDto.getName());
         return userService.updateUserByID(userUpdateRequestDto);
     }

     @PostMapping("/addRole")
     public ResponseEntity<ApiResponse<String>> addUserRoleById(@Valid @RequestBody RoleRequestDto roleRequestDto){
         return  userService.addUserRoleById(roleRequestDto);
     }

}
