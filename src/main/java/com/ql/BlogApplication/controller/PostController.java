package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.PostRequestDto;
import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/post")

public class PostController {

       PostService postService;

       @Autowired
       PostController(PostService postService){
           this.postService=postService;
       }

       @GetMapping("/all")
       ResponseEntity<ApiResponse<List<Post>>> getAllPosts(){
                return postService.getAllPosts();
       }

       @PostMapping("/create")
       ResponseEntity<ApiResponse<String>> createPost(@Valid @RequestBody PostRequestDto postRequestDto) {
           return postService.createPost(postRequestDto);
       }

       @PostMapping("/upload-image/{id}")
       ResponseEntity<ApiResponse<String>> uploadImage(@PathVariable String id,@RequestParam("file") MultipartFile multipartFile){
          return postService.uploadImage(id,multipartFile);
       }


       @GetMapping("/{category}")
       ResponseEntity<ApiResponse<List<Post>>> findAllPostByCategory(@PathVariable String category){
               return postService.findAllPostByCategory(category);
       }

       @PutMapping("/publish/{id}")
       ResponseEntity<ApiResponse<String>> publishPost(@PathVariable String id){
            return postService.publishPost(id);
       }


       @GetMapping("/getAllComments/{id}")
       ResponseEntity<ApiResponse<List<Comment>>> findAllCommentByPostId(@PathVariable String id){
               return postService.findAllCommentByPostId(id);
       }

       @GetMapping("/delete/{id}")
       ResponseEntity<ApiResponse<String>> deletePostByPostId(@PathVariable String id){
              return postService.deletePostByPostId(id);
       }

}
