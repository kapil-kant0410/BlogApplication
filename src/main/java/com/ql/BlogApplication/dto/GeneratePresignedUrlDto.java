package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePresignedUrlDto {

    @NotBlank(message = "File name must not be blank")
    private String fileName;

    @NotBlank(message = "Content type must not be blank")
    @Pattern(
            regexp = "^[a-zA-Z0-9]+/[a-zA-Z0-9\\-\\.\\+]+$",
            message = "Invalid content type format (expected something like 'image/png' or 'application/json')"
    )
    private String contentType;

}
