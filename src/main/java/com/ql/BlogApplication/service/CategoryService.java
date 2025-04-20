package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CategoryRequestDto;
import com.ql.BlogApplication.entity.Category;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.exception.CategoryNotFoundException;
import com.ql.BlogApplication.repository.CategoryRepository;
import com.ql.BlogApplication.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    //returns all category list from the category table
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategory(){
         List<Category> allCategories  = categoryRepository.findAll();
         ApiResponse<List<Category>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),allCategories,MessageCodes.messages.get(134));
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
           ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(131),MessageCodes.messages.get(131));
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //delete a category and assign all post under it as uncategorized
    public ResponseEntity<ApiResponse<String>> deleteCategory(String id) {

        Category category=categoryRepository.findById(id).orElseThrow(()->new CategoryNotFoundException(MessageCodes.messages.get(231)));
        Category uncategorized=categoryRepository.findByName("uncategorized").orElseThrow(()->new CategoryNotFoundException(MessageCodes.messages.get(231)));

        List<String> postIds=category.getPostIds();
        List<Post>   posts=postRepository.findAllById(postIds);

        for(Post post:posts){
              post.setCategoryId(uncategorized.getId());
              uncategorized.getPostIds().add(post.getId());
              categoryRepository.save(uncategorized);
        }

        postRepository.saveAll(posts);
        categoryRepository.deleteById(id);

        ApiResponse<String> apiResponse= ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(133),MessageCodes.messages.get(133));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
