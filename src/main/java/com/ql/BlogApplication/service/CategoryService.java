package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CategoryRequestDto;
import com.ql.BlogApplication.dto.CategoryResponseDto;
import com.ql.BlogApplication.entity.Category;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.exception.CategoryNotFoundException;
import com.ql.BlogApplication.mapper.CategoryMapper;
import com.ql.BlogApplication.repository.CategoryRepository;
import com.ql.BlogApplication.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CategoryMapper categoryMapper;

    //returns all category list from the category table
    public ResponseEntity<ApiResponse<Map<String, List<CategoryResponseDto>>>> getAllCategory(){
         List<Category> allCategory= categoryRepository.findAll();
         List<CategoryResponseDto> categoryResponseDtos=categoryMapper.toDtoList(allCategory);
         Map<String,List<CategoryResponseDto>> data=new HashMap<>();
         data.put("All Categories",categoryResponseDtos);
         ApiResponse<Map<String, List<CategoryResponseDto>>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,MessageCodes.messages.get(134));
         return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //check for role comes in response, duplicate not allowed and one space between words is allowed
    public ResponseEntity<ApiResponse<Map<String,CategoryResponseDto>>> createCategory(CategoryRequestDto categoryRequestDto){

           Category category=new Category();
           category.setName(categoryRequestDto.getName().trim().toLowerCase());

           if(Boolean.TRUE.equals(categoryRepository.existsByName(category.getName()))){
                ApiResponse<Map<String,CategoryResponseDto>> apiResponse= ApiResponse.error(HttpStatus.CONFLICT.value(),null,"Category already exists.");
                return new ResponseEntity<>(apiResponse,HttpStatus.CONFLICT);
           }

           categoryRepository.save(category);
           CategoryResponseDto categoryResponseDto=categoryMapper.toDto(category);

           Map<String,CategoryResponseDto> data=new HashMap<>();
           data.put("New Category",categoryResponseDto);

           ApiResponse<Map<String,CategoryResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), data,MessageCodes.messages.get(131));
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //delete a category and assign all post under it as uncategorized
    public ResponseEntity<ApiResponse<Map<String,CategoryResponseDto>>> deleteCategory(Long id) {
        Category category=categoryRepository.findById(id).orElseThrow(()->new CategoryNotFoundException(MessageCodes.messages.get(231)));
        Category uncategorized=categoryRepository.findByName("uncategorized").orElseThrow(()->new CategoryNotFoundException(MessageCodes.messages.get(231)));

        List<Post> posts=category.getPosts();

        for(Post post:posts){
              post.setCategory(uncategorized);
              postRepository.save(post);
        }

        categoryRepository.deleteById(id);

        CategoryResponseDto categoryResponseDto=categoryMapper.toDto(category);
        Map<String,CategoryResponseDto> data=new HashMap<>();
        data.put("Category deleted",categoryResponseDto);

        ApiResponse<Map<String,CategoryResponseDto>> apiResponse= ApiResponse.success(HttpStatus.OK.value(),data,MessageCodes.messages.get(133));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
