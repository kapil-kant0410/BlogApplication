package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.PostRequestDto;
import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.*;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.interceptor.AuthorInterceptor;
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
import jakarta.servlet.http.HttpServletRequest;
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

          Optional<Category> category=categoryRepository.findById(postRequestDto.getCategoryId());

          if(category.isEmpty()){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No category found","No category found");
              return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
          }

          Post newPost=new Post();
          newPost.setTitle(postRequestDto.getTitle());
          newPost.setContent(postRequestDto.getContent());
          newPost.setImageURL(postRequestDto.getImageUrl());

          String token= TokenContext.getToken();
          Long id= Long.parseLong(jwtUtil.extractId(token));
          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(106)));
          newPost.setAuthor(user);
          newPost.setCategory(category.get());
          postRepository.save(newPost);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"post saved successfully.","post saved successfully.");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      public ResponseEntity<ApiResponse<String>> uploadImage(Long id,MultipartFile multipartFile) {

        try{

            String fileName= UUID.randomUUID()+"_"+multipartFile.getOriginalFilename();
            Optional<Post> optionalPost=postRepository.findById(id);

            if(optionalPost.isEmpty()){
                ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"post does not exists","post does not exists");
                return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
            }

            Path uploadPath= Paths.get(uploadDir);

            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            Path filePath=uploadPath.resolve(fileName);
            Files.copy(multipartFile.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl="http://localhost:8080/uploads/"+fileName;

            logger.info("URL for the image: {}", imageUrl);

            optionalPost.get().setImageURL(imageUrl);
            postRepository.save(optionalPost.get());
            logger.info("Image saved successfully");

        }catch (IOException ioException){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"post does not exists","post does not exists");
            return new ResponseEntity<>(apiResponse,HttpStatus.INTERNAL_SERVER_ERROR);
        }

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"image uploaded successfully","image uploaded successfully");
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

          Optional<Post> optionalPost=postRepository.findById(id);

          if(optionalPost.isEmpty()){
              ApiResponse<List<CommentResponseDto>> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),null, "No post found.");
              return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
          }

            Post post=optionalPost.get();
            List<Comment> comments=post.getComments();

            List<CommentResponseDto> allComments= CommentMapper.toDtoList(comments);

          ApiResponse<List<CommentResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),allComments, "All comments for a post");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //publish a post if not published
      public ResponseEntity<ApiResponse<String>> publishPost(Long id){
          Optional<Post> optionalPost=postRepository.findById(id);
          logger.info("publishing a post");

          if(optionalPost.isEmpty()){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"No post found", "No post found");
              return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
          }

          Post post=optionalPost.get();

          if(post.getIsPublished()==Boolean.TRUE){
              logger.info("setting post published false");
              post.setIsPublished(false);
              postRepository.save(post);
              logger.info("successfully unpublished");
              ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Post unpublish successfully", "Post unpublish successfully");
              return new ResponseEntity<>(apiResponse,HttpStatus.OK);
          }

          logger.info("setting post published true");
          post.setIsPublished(true);
          postRepository.save(post);
          logger.info("successfully published");

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Post published successfully", "Post published successfully");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }
}
