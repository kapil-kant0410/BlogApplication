package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.dto.UserResponseDto;
import com.ql.BlogApplication.entity.User;
import java.util.List;

public class UserMapper {

   public static List<UserResponseDto> toDtoList(List<User> users){
          return  users.stream().map(user -> {
              return UserResponseDto.builder()
                      .Id(user.getId())
                      .name(user.getName())
                      .email(user.getEmail())
                      .build();
          }).toList();
    }

    public static UserResponseDto toDto(User user){
        return UserResponseDto.builder()
                .Id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

}
