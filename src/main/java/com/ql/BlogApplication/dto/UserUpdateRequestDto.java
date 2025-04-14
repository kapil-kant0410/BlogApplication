package com.ql.BlogApplication.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    @NotBlank(message = "Name field must not be empty")
    @Pattern(regexp = "^[A-Za-z]+([\\s][A-Za-z]+)*$", message = "Name must contain only alphabets and spaces with no trailing and leading space only one space between words")
    private String name;

    @Email(message = "Invalid email field")
    private String email;

    @NotBlank(message = "Password field must not be empty")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character")
    private String password;

}
