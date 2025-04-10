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
import java.util.Optional;


@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    //returns all category list from the category table
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategory(){
         List<Category> allCategory= categoryRepository.findAll();
         ApiResponse<List<Category>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),allCategory,"Category fetched successfully");
         return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //check for role comes in response, duplicate not allowed and one space between words is allowed
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

    //delete a category and all posts under this category
    public ResponseEntity<ApiResponse<String>> deleteCategory(Long id) {
        Optional<Category> optionalCategory=categoryRepository.findById(id);

        if(optionalCategory.isEmpty()){
            ApiResponse<String> apiResponse= ApiResponse.error(HttpStatus.NOT_FOUND.value(),"Category not found","Category not found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        categoryRepository.deleteById(id);
        ApiResponse<String> apiResponse= ApiResponse.success(HttpStatus.OK.value(),"Category deleted successfully","Category deleted successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
