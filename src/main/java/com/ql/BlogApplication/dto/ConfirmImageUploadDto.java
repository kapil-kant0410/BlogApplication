package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ConfirmImageUploadDto {
    @NotBlank
    String imageUrl;
}
