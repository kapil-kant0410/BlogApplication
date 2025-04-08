package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.dto.UserResponseDto;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;

import java.util.List;
import java.util.Set;

public class UserMapper {

   public static List<String> findUserRoles(Set<UserRole> userRoleSet){

      return  userRoleSet.stream().map(userRole -> {
          return userRole.getRole().getName();
       }).toList();

   }

   public static List<UserResponseDto> toDtoList(List<User> users){
          return  users.stream().map(user -> {
              return UserResponseDto.builder()
                      .name(user.getName())
                      .email(user.getEmail())
                      .role(findUserRoles(user.getUserRoles()))
                      .build();
          }).toList();
    }

    public static UserResponseDto toDto(User user){
        return UserResponseDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(findUserRoles(user.getUserRoles()))
                .build();
    }

}
