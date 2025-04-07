package com.ql.BlogApplication.service;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CategoryRequestDto;
import com.ql.BlogApplication.entity.Category;
import com.ql.BlogApplication.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    //working fine
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategory(){
         List<Category> allCategory= categoryRepository.findAll();
         ApiResponse<List<Category>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),allCategory,"Category fetched successfully");
         return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //check for role comes in response, duplicate not allowed and one space between words is allowed.
    public ResponseEntity<ApiResponse<String>> createCategory(CategoryRequestDto categoryRequestDto){

           Category category=new Category();
           category.setName(categoryRequestDto.getName().trim().toLowerCase());

           if(Boolean.TRUE.equals(categoryRepository.existsByName(category.getName()))){
                ApiResponse<String> apiResponse= ApiResponse.error(HttpStatus.CONFLICT.value(),"Category already exists.","Category already exists.");
                return new ResponseEntity<>(apiResponse,HttpStatus.CONFLICT);
           }

           categoryRepository.save(category);
           ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Category created successfully","Category created successfully");
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
