package com.ql.BlogApplication.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthorSubscribersResponseDto {
    private  Long userId;
    private  String name;
    private String email;
}
