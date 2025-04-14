package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.PostRequestDto;
import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.service.PostService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post")
@AllArgsConstructor

public class PostController {

       PostService postService;

       @GetMapping("/all")
       ResponseEntity<ApiResponse<List<PostResponseDto>>> getAllPosts(){
                return postService.getAllPosts();
       }

       @PostMapping("/create")
       ResponseEntity<ApiResponse<String>> createPost(@Valid @RequestBody PostRequestDto postRequestDto){
               return postService.createPost(postRequestDto);
       }

       @GetMapping("/{category}")
       ResponseEntity<ApiResponse<List<PostResponseDto>>> findAllPostByCategory(@PathVariable String category){
               return postService.findAllPostByCategory(category);
       }

       @GetMapping("/getAllComments/{id}")
       ResponseEntity<ApiResponse<List<CommentResponseDto>>> findAllCommentByPostId(@PathVariable Long id){
               return postService.findAllCommentByPostId(id);
       }

}
