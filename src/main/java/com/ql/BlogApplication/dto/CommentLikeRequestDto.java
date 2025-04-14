package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentLikeRequestDto {

    @NotNull(message = "Comment Id must not be null")
    @Min(value = 1, message = "Comment Id must be greater than or equal to 1")
    private Long commentId;

}
