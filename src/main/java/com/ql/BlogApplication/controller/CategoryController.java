package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CategoryRequestDto;
import com.ql.BlogApplication.dto.CategoryResponseDto;
import com.ql.BlogApplication.entity.Category;
import com.ql.BlogApplication.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
@AllArgsConstructor

public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/all")
    ResponseEntity<ApiResponse<Map<String, List<CategoryResponseDto>>>> getAllCategory(){
        return categoryService.getAllCategory();
    }


    @PostMapping("/create")
    ResponseEntity<ApiResponse<Map<String,CategoryResponseDto>>> createCategory(@Valid @RequestBody CategoryRequestDto categoryRequestDto){
        return categoryService.createCategory(categoryRequestDto);
    }

    @DeleteMapping("/delete/{id}")
    ResponseEntity<ApiResponse<Map<String,CategoryResponseDto>>> deleteCategory(@Valid @PathVariable Long id){
        return categoryService.deleteCategory(id);
    }

}
