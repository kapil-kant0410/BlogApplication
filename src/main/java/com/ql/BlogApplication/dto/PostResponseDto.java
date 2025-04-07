package com.ql.BlogApplication.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostResponseDto {
   private  String title;
   private  String content;

}
