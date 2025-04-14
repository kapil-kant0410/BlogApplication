package com.ql.BlogApplication.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserSubscribedAuthorResponseDto {

   private  Long authorId;
   private  String name;
   private String email;

}
