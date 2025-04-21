package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.*;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.RoleNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.mapper.UserMapper;
import com.ql.BlogApplication.repository.*;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {

      Logger logger= LoggerFactory.getLogger(UserService.class);

      private final UserRepository userRepository;
      private final RoleRepository roleRepository;
      private final JwtUtil jwtUtil;
      private final PostRepository postRepository;
      private final CommentRepository commentRepository;
      private final LikeRepository likeRepository;
      private final UserMapper userMapper;

      //Working properly
      public UserService(UserMapper userMapper,LikeRepository likeRepository,CommentRepository commentRepository,PostRepository postRepository,UserRepository userRepository, RoleRepository roleRepository,JwtUtil jwtUtil){
          this.userRepository=userRepository;
          this.roleRepository=roleRepository;
          this.jwtUtil=jwtUtil;
          this.postRepository=postRepository;
          this.commentRepository=commentRepository;
          this.likeRepository=likeRepository;
          this.userMapper=userMapper;
      }

      //Working properly
      public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers(){
        List<User> allUsers= userRepository.findAll();
        List<UserResponseDto> userResponseDtoList=userMapper.toDtoList(allUsers);
        ApiResponse<List<UserResponseDto>> apiResponse= ApiResponse.<List<UserResponseDto>>success(HttpStatus.OK.value(), userResponseDtoList,"All users fetched successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //Working properly
      public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(String id){
          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
          UserResponseDto userResponseDto=userMapper.toDto(user);
          ApiResponse<UserResponseDto> apiResponse= ApiResponse.<UserResponseDto>success(HttpStatus.OK.value(), userResponseDto,MessageCodes.messages.get(105));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //working properly
      public ResponseEntity<ApiResponse<String>> updateUserByID( UserUpdateRequestDto userUpdateRequestDto ){

            String token= TokenContext.getToken();
            String id=jwtUtil.extractId(token);

            User userToUpdate=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

            boolean isUpdated=false;

            if(!Objects.equals(userToUpdate.getName(), userUpdateRequestDto.getName())){
                userToUpdate.setName(userUpdateRequestDto.getName());
                isUpdated=true;
            }

            if(!Objects.equals(userToUpdate.getPassword(), userUpdateRequestDto.getPassword())){
              userToUpdate.setPassword(userUpdateRequestDto.getPassword());
              isUpdated=true;
            }

            if(!isUpdated){
              ApiResponse<String> apiResponse= ApiResponse.<String>success(HttpStatus.OK.value(), "No changes made","No changes made");
              return new ResponseEntity<>(apiResponse,HttpStatus.OK);
            }

            userRepository.save(userToUpdate);

           ApiResponse<String> apiResponse= ApiResponse.<String>success(HttpStatus.OK.value(), MessageCodes.messages.get(103),MessageCodes.messages.get(103));
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //Working properly
      public ResponseEntity<ApiResponse<String>> deleteUserById(){

        String token= TokenContext.getToken();
        String userId= jwtUtil.extractId(token);

        User user = userRepository.findById(userId)
                  .orElseThrow(() -> new UserNotFoundException(MessageCodes.messages.get(201)));

        List<Post> posts=postRepository.findByAuthorId(userId);

        for(Post post:posts){
            commentRepository.deleteAllById(post.getCommentIds());
            likeRepository.deleteAllById(post.getLikeIds());
            postRepository.delete(post);
        }

        commentRepository.deleteAllById(user.getCommentIds());
        likeRepository.deleteAllById(user.getLikeIds());

        for(String authorId:user.getSubscribedAuthorIds()){
           User author=userRepository.findById(authorId).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
           author.getSubscriberIds().remove(userId);
           userRepository.save(author);
        }

        for(String subscriberId:user.getSubscriberIds()){
            User subscriber=userRepository.findById(subscriberId).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
            subscriber.getSubscribedAuthorIds().remove(userId);
            userRepository.save(subscriber);
        }

        userRepository.deleteById(userId);
        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(104),MessageCodes.messages.get(104));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

      //Working properly
      public ResponseEntity<ApiResponse<String>> addUserRoleById(RoleRequestDto roleRequestDto){

          String token= TokenContext.getToken();
          String id= jwtUtil.extractId(token);

          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
          Role role=roleRepository.findByName(roleRequestDto.getRole()).orElseThrow(()->new RoleNotFoundException(MessageCodes.messages.get(211)));

          boolean hasRole=user.getRoleIds().contains(role.getId());

          if(hasRole){
              ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.CONFLICT.value(),"User already has this role","User already has this role");
              return new ResponseEntity<>(apiResponse,HttpStatus.CONFLICT);
          }

          user.getRoleIds().add(role.getId());
          userRepository.save(user);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(111),MessageCodes.messages.get(111));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

}
