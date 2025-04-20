package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.PostRequestDto;
import com.ql.BlogApplication.entity.*;
import com.ql.BlogApplication.exception.CategoryNotFoundException;
import com.ql.BlogApplication.exception.PostNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.repository.*;
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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


@Service
public class PostService {

      private final PostRepository postRepository;
      private final UserRepository userRepository;
      private final CategoryRepository categoryRepository;
      private final CommentRepository commentRepository;
      private final LikeRepository likeRepository;
      private final JwtUtil jwtUtil;
      @Value("${file.uploads-dir}")
      private String uploadDir;
      private static final Logger logger = LoggerFactory.getLogger(PostService.class);


    PostService(LikeRepository likeRepository,PostRepository postRepository,CommentRepository commentRepository, UserRepository userRepository, CategoryRepository categoryRepository, JwtUtil jwtUtil){
          this.postRepository=postRepository;
          this.userRepository=userRepository;
          this.categoryRepository=categoryRepository;
          this.jwtUtil=jwtUtil;
          this.commentRepository=commentRepository;
          this.likeRepository=likeRepository;
      }

      //working fine getting all post
      public ResponseEntity<ApiResponse<List<Post>>> getAllPosts(){
          List<Post> allPosts=postRepository.findByIsPublishedTrue();
          ApiResponse<List<Post>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),allPosts,"Posts fetched successfully.");
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
          String id= jwtUtil.extractId(token);
          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));

          newPost.setAuthorId(user.getId());
          newPost.setCategoryId(category.getId());
          postRepository.save(newPost);

          user.getPostIds().add(newPost.getId());
          userRepository.save(user);
          category.getPostIds().add(newPost.getId());
          categoryRepository.save(category);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(121),MessageCodes.messages.get(121));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      public ResponseEntity<ApiResponse<String>> uploadImage(String id,MultipartFile multipartFile) {

        try{

            String token= TokenContext.getToken();
            String userId= jwtUtil.extractId(token);

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
       public ResponseEntity<ApiResponse<String>> publishPost(String id){

        String token= TokenContext.getToken();
        String userId= jwtUtil.extractId(token);

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
      public ResponseEntity<ApiResponse<List<Post>>> findAllPostByCategory(String category){
            Category categoryEntity=categoryRepository.findByName(category).orElseThrow(()-> new CategoryNotFoundException(MessageCodes.messages.get(231)));
            List<Post> posts=postRepository.findByCategoryIdAndIsPublishedTrue(categoryEntity.getId());
            ApiResponse<List<Post>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),posts,"All posts inside category");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //working fine getting all comment list under a post
      public ResponseEntity<ApiResponse<List<Comment>>> findAllCommentByPostId(String id){

          String token= TokenContext.getToken();
          String userId= jwtUtil.extractId(token);

          Post post=postRepository.findByAuthorIdAndId(userId,id).orElseThrow(()->new PostNotFoundException(MessageCodes.messages.get(221)));
          Set<String> commentIds=post.getCommentIds();
          List<Comment> comments=commentRepository.findAllById(commentIds);

          ApiResponse<List<Comment>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),comments, "All comments for a post");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      public ResponseEntity<ApiResponse<String>> deletePostByPostId(String postId){

           String token=TokenContext.getToken();
           String userId=jwtUtil.extractId(token);

           Post post=postRepository.findById(postId).orElseThrow(()-> new PostNotFoundException(MessageCodes.messages.get(221)));

           if(!Objects.equals(post.getAuthorId(), userId)){
               ApiResponse<String> response = ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Unauthorized", "You are not the author of this post.");
               return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
           }

           if(post.getCommentIds()!=null && !post.getCommentIds().isEmpty()){
               commentRepository.deleteAllById(post.getCommentIds());
           }

           if(post.getLikeIds()!=null && !post.getLikeIds().isEmpty()){
               likeRepository.deleteAllById(post.getLikeIds());
           }

           User user=userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
           user.getPostIds().remove(postId);
           userRepository.save(user);

           Category category=categoryRepository.findById(post.getCategoryId()).orElseThrow(()->new CategoryNotFoundException(MessageCodes.messages.get(231)));
           category.getPostIds().remove(postId);
           categoryRepository.save(category);

           postRepository.deleteById(postId);
           ApiResponse<String> response = ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(123), MessageCodes.messages.get(123));
           return new ResponseEntity<>(response, HttpStatus.OK);
      }

}
