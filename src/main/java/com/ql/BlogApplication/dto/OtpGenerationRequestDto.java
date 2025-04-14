package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OtpGenerationRequestDto {
    @Email(message = "Please enter valid email")
    private String email;
}
