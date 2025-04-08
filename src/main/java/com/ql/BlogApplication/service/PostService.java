package com.ql.BlogApplication.service;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.PostRequestDto;
import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.*;
import com.ql.BlogApplication.mapper.CommentMapper;
import com.ql.BlogApplication.mapper.PostMapper;
import com.ql.BlogApplication.repository.CategoryRepository;
import com.ql.BlogApplication.repository.PostRepository;
import com.ql.BlogApplication.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;



@Service
public class PostService {

      private final PostRepository postRepository;
      private final UserRepository userRepository;
      private final CategoryRepository categoryRepository;

      PostService(PostRepository postRepository, UserRepository userRepository, CategoryRepository categoryRepository,PostMapper postMapper){
          this.postRepository=postRepository;
          this.userRepository=userRepository;
          this.categoryRepository=categoryRepository;
      }

      //working fine getting all post
      public ResponseEntity<ApiResponse<List<PostResponseDto>>> getAllPosts(){

          List<Post> allPosts=postRepository.findAll();
          List<PostResponseDto> postResponseDtoList=PostMapper.toDtoList(allPosts);

          ApiResponse<List<PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),postResponseDtoList,"Posts fetched successfully.");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);

      }

      //working fine check for same post
      public ResponseEntity<ApiResponse<String>> createPost(PostRequestDto postRequestDto){

          Optional<User> user=userRepository.findById(postRequestDto.getAuthorId());
          Optional<Category> category=categoryRepository.findById(postRequestDto.getCategoryId());

          if(user.isEmpty()){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No author found","No author found");
              return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
          }

          if(category.isEmpty()){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No category found","No category found");
              return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
          }

          Set<UserRole> userRoles=user.get().getUserRoles();

          boolean hasAuthorRole = userRoles.stream()
                  .map(userRole -> userRole.getRole().getName())
                  .anyMatch("author"::equals);

          if(!hasAuthorRole){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.FORBIDDEN.value(), "provided id is not an author","provided id is not an author");
              return new ResponseEntity<>(apiResponse,HttpStatus.FORBIDDEN);
          }

          Post newPost=new Post();
          newPost.setTitle(postRequestDto.getTitle());
          newPost.setContent(postRequestDto.getContent());
          newPost.setAuthor(user.get());
          newPost.setCategory(category.get());

          postRepository.save(newPost);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"post saved successfully.","post saved successfully.");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //working fine getting all post under a category
      public ResponseEntity<ApiResponse<List<PostResponseDto>>> findAllPostByCategory(String category){

            List<Post> allPosts=postRepository.findAll();

            List<Post> filterPosts= allPosts.stream().filter(post->post.getCategory().getName().equals(category)).toList();

            List<PostResponseDto>  postResponseDtoList = PostMapper.toDtoList(filterPosts);

            ApiResponse<List<PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),postResponseDtoList,"All posts.");
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

}
