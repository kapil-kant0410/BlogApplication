package com.ql.BlogApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequestDto {
     @NotBlank(message = "Category name cannot be blank")
     @Pattern(regexp = "^[A-Za-z0-9&\\-_]+( [A-Za-z0-9&\\-_]+)*$",
             message = "Category name can contain letters, numbers, &, -, _, and must have a single space between words without leading or trailing spaces.")
     @Size(min = 3, max = 50, message = "Category name must be between 3 and 50 characters.")
     private String name;
}
