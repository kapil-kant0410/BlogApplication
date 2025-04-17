package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentUpdateRequestDto {

    @NotBlank(message = "content field must not be empty")
    private String content;

}
