package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PostLikeRequestDto {

    @NotNull(message = "Post Id must not be null")
    private String postId;

}
