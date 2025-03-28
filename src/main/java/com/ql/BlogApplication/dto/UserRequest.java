package com.ql.BlogApplication.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @Min(value = 1,message = "Id must be at least 1.")
    @NotNull(message = "Id must not be null")
    private int id;

    @Min(value = 18,message = "Age must be at least 18.")
    @Max(value = 100,message = "Age must be at most 100.")
    @NotNull(message = "Age must be required")
    private int age;
    @NotBlank(message = "message field must not be empty.")
    private String message;
}
