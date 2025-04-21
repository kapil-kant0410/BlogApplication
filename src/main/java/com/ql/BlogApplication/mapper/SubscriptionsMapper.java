package com.ql.BlogApplication.mapper;
import com.ql.BlogApplication.dto.UserSubscribedAuthorResponseDto;
import com.ql.BlogApplication.entity.User;

import java.util.List;

public class SubscriptionsMapper {

    public static List<UserSubscribedAuthorResponseDto> toDtoList(List<User> subscribedAuthors){
       return subscribedAuthors.stream().map(user -> {
            return UserSubscribedAuthorResponseDto.builder()
                    .authorId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .build();
        }).toList();
    }

}
