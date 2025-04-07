package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestDto {
    @NotBlank(message = "Role name must not be empty")
    @Pattern(regexp = "author|viewer", message = "Role name must be either 'author' or 'viewer'")
    private String role;
}
