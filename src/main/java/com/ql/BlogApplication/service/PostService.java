package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.PostRequestDto;
import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.*;
import com.ql.BlogApplication.exception.CategoryNotFoundException;
import com.ql.BlogApplication.exception.PostNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.mapper.CommentMapper;
import com.ql.BlogApplication.mapper.PostMapper;
import com.ql.BlogApplication.repository.CategoryRepository;
import com.ql.BlogApplication.repository.PostRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class PostService {

      private final PostRepository postRepository;
      private final UserRepository userRepository;
      private final CategoryRepository categoryRepository;
      private final JwtUtil jwtUtil;
      @Value("${file.uploads-dir}")
      private String uploadDir;
      private static final Logger logger = LoggerFactory.getLogger(PostService.class);


    PostService(PostRepository postRepository, UserRepository userRepository, CategoryRepository categoryRepository, JwtUtil jwtUtil){
          this.postRepository=postRepository;
          this.userRepository=userRepository;
          this.categoryRepository=categoryRepository;
          this.jwtUtil=jwtUtil;
      }

      //working fine getting all post
      public ResponseEntity<ApiResponse<List<PostResponseDto>>> getAllPosts(){

          List<Post> allPosts=postRepository.findByIsPublishedTrue();
          List<PostResponseDto> postResponseDtoList=PostMapper.toDtoList(allPosts);

          ApiResponse<List<PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),postResponseDtoList,"Posts fetched successfully.");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);

      }

      //working fine check for same post
      public ResponseEntity<ApiResponse<String>> createPost(PostRequestDto postRequestDto) {

          Category category=categoryRepository.findById(postRequestDto.getCategoryId()).orElseThrow(()-> new CategoryNotFoundException(MessageCodes.messages.get(231)));

          Post newPost=new Post();
          newPost.setTitle(postRequestDto.getTitle());
          newPost.setContent(postRequestDto.getContent());
          newPost.setImageURL(postRequestDto.getImageUrl());

          String token= TokenContext.getToken();
          Long id= Long.parseLong(jwtUtil.extractId(token));
          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
          newPost.setAuthor(user);
          newPost.setCategory(category);
          postRepository.save(newPost);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(121),MessageCodes.messages.get(121));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      public ResponseEntity<ApiResponse<String>> uploadImage(Long id,MultipartFile multipartFile) {

        try{

            String token= TokenContext.getToken();
            Long userId= Long.parseLong(jwtUtil.extractId(token));

            String fileName= UUID.randomUUID()+"_"+multipartFile.getOriginalFilename();
            Post post=postRepository.findByAuthorIdAndId(userId,id).orElseThrow(()-> new PostNotFoundException(MessageCodes.messages.get(221)));

            Path uploadPath= Paths.get(uploadDir);

            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            Path filePath=uploadPath.resolve(fileName);
            Files.copy(multipartFile.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl="http://localhost:8080/uploads/"+fileName;

            logger.info("URL for the image: {}", imageUrl);

            post.setImageURL(imageUrl);
            postRepository.save(post);
            logger.info("Image saved successfully");

        }catch (IOException ioException){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Internal server error","internal server error");
            return new ResponseEntity<>(apiResponse,HttpStatus.INTERNAL_SERVER_ERROR);
        }

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"image uploaded successfully","image uploaded successfully");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

       //publish a post if not published
       public ResponseEntity<ApiResponse<String>> publishPost(Long id){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        Post post=postRepository.findByAuthorIdAndId(userId,id).orElseThrow(()-> new PostNotFoundException(MessageCodes.messages.get(221)));

        if(post.getIsPublished()==Boolean.TRUE){
            post.setIsPublished(false);
            postRepository.save(post);
            ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Post unpublish successfully", "Post unpublish successfully");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        }

        post.setIsPublished(true);
        postRepository.save(post);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Post published successfully", "Post published successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

      //working fine getting all post under a category
      public ResponseEntity<ApiResponse<List<PostResponseDto>>> findAllPostByCategory(String category){

            List<Post> allPosts=postRepository.findByIsPublishedTrue();

            List<Post> filterPosts= allPosts.stream().filter(post->post.getCategory().getName().equals(category)).toList();

            List<PostResponseDto>  postResponseDtoList = PostMapper.toDtoList(filterPosts);

            ApiResponse<List<PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),postResponseDtoList,"All posts inside category");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //working fine getting all comment list under a post
      public ResponseEntity<ApiResponse<List<CommentResponseDto>>> findAllCommentByPostId(Long id){

          String token= TokenContext.getToken();
          Long userId= Long.parseLong(jwtUtil.extractId(token));

          Post post=postRepository.findByAuthorIdAndId(userId,id).orElseThrow(()->new PostNotFoundException(MessageCodes.messages.get(221)));

          List<Comment> comments=post.getComments();

          List<CommentResponseDto> allComments= CommentMapper.toDtoList(comments);

          ApiResponse<List<CommentResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),allComments, "All comments for a post");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

}
