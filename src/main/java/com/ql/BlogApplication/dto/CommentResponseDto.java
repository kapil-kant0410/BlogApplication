package com.ql.BlogApplication.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentResponseDto {
     private Long id;
     private String name;
     private String content;
}
