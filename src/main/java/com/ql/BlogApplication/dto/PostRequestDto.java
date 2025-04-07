package com.ql.BlogApplication.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class PostRequestDto {

    @Pattern(regexp = "^[A-Za-z0-9.,!? ]+$", message = "Title can contain letters, numbers, spaces, and basic punctuation (.,!?).")
    @NotBlank(message = "Title must not be empty")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters.")
    private String title;

    @NotBlank(message = "Content must not be empty")
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters.")
    private String content;

    @NotNull(message = "Author ID must not be null")
    @Positive(message = "Author ID must be a positive number")
    private Long authorId;

    @NotNull(message = "Category ID must not be null")
    @Positive(message = "Category ID must be a positive number")
    private Long categoryId;

}
