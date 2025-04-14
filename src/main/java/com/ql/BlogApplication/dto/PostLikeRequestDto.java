package com.ql.BlogApplication.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PostLikeRequestDto {

    @NotNull(message = "User Id must not be null")
    @Min(value = 1, message = "User Id must be greater than or equal to 1")
    private Long userId;

    @NotNull(message = "Post Id must not be null")
    @Min(value = 1, message = "User Id must be greater than or equal to 1")
    private Long postId;
}
