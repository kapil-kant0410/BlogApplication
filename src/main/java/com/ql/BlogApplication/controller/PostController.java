package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.*;
import com.ql.BlogApplication.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post")

public class PostController {

       PostService postService;

       @Autowired
       PostController(PostService postService){
           this.postService=postService;
       }


       @GetMapping("/all")
       ResponseEntity<ApiResponse<Map<String,List<PostResponseDto>>>> getAllPosts(){
                return postService.getAllPosts();
       }

       @PostMapping("/create")
       ResponseEntity<ApiResponse<Map<String,PostResponseDto>>> createPost(@Valid @RequestBody PostRequestDto postRequestDto) {
           return postService.createPost(postRequestDto);
       }

       @PostMapping("/upload-image/{id}")
       ResponseEntity<ApiResponse<Map<String,PostResponseDto>>> uploadImage(@PathVariable Long id,@RequestParam("file") MultipartFile multipartFile){
          return postService.uploadImage(id,multipartFile);
       }

       @PostMapping("/generate-presignedUrl")
       ResponseEntity<ApiResponse<Map<String,String>>>  generatePreSignedUrl(@Valid @RequestBody GeneratePresignedUrlDto generatePresignedUrlDto ){
          return postService.generatePreSignedUrl(generatePresignedUrlDto.getFileName(),generatePresignedUrlDto.getContentType());
       }

       @PostMapping("confirm-image-upload/{postId}")
       ResponseEntity<ApiResponse<Map<String,PostResponseDto>>>  confirmImageUpload(@PathVariable Long postId, @Valid @RequestBody ConfirmImageUploadDto confirmImageUploadDto){
           return postService.confirmImageUpload(postId,confirmImageUploadDto.getImageUrl());
       }

       @GetMapping("/{category}")
       ResponseEntity<ApiResponse<Map<String,List<PostResponseDto>>  >> findAllPostByCategory(@PathVariable String category){
               return postService.findAllPostByCategory(category);
       }

       @PutMapping("/publish/{id}")
       ResponseEntity<ApiResponse<Map<String,PostResponseDto>>> publishPost(@PathVariable Long id){
            return postService.publishPost(id);
       }


       @GetMapping("/getAllComments/{id}")
       ResponseEntity<ApiResponse<Map<String,List<CommentResponseDto>>>> findAllCommentByPostId(@PathVariable Long id){
               return postService.findAllCommentByPostId(id);
       }

}
