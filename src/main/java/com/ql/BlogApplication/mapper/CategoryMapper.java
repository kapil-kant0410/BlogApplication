package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.dto.CategoryResponseDto;
import com.ql.BlogApplication.entity.Category;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CategoryMapper {

    public List<CategoryResponseDto> toDtoList(List<Category> categoryList){
          return categoryList.stream().map(category -> CategoryResponseDto.builder().name(category.getName()).build()).toList();
    }

    public CategoryResponseDto toDto(Category category){
        return CategoryResponseDto.builder().name(category.getName()).build();
    }
}
