package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class CommentRequestDto {
      
      @NotBlank(message = "content field must not be empty")
      private String content;

      @NotNull(message="User Id required.")
      private  Long userId;

      @NotNull(message = "Post Id required.")
      private Long postId;
}
