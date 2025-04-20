package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentLikeRequestDto {

    @NotNull(message = "Comment Id must not be null")
    private String commentId;

}
