package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserValidateOtpLoginRequestDto {
    @Email(message = "Please pass valid email.")
    private String email;

    @NotBlank(message = "Password field must not be empty")
    private String otp;
}
