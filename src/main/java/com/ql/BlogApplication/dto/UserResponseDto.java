package com.ql.BlogApplication.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponseDto {

    private Long Id;
    private String name;
    private String email;

}

