package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.dto.AuthorSubscribersResponseDto;
import com.ql.BlogApplication.entity.User;

import java.util.List;
import java.util.Set;

public class SubscriberMapper {

    public static List<AuthorSubscribersResponseDto> toDtoList(List<User> authorSubscribers){
        return authorSubscribers.stream().map(user->{
            return AuthorSubscribersResponseDto.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .build();
        }).toList();
    }

}
