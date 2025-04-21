package com.ql.BlogApplication.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthorSubscribersResponseDto {
    private  String userId;
    private  String name;
    private String email;
}
