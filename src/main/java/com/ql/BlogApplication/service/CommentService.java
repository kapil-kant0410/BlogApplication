package com.ql.BlogApplication.service;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentRequestDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.repository.CommentRepository;
import com.ql.BlogApplication.repository.PostRepository;
import com.ql.BlogApplication.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    CommentService(CommentRepository commentRepository,UserRepository userRepository,PostRepository postRepository){
        this.commentRepository=commentRepository;
        this.userRepository=userRepository;
        this.postRepository=postRepository;
    }

    //working properly creating a comment
    public ResponseEntity<ApiResponse<String>> createComment(CommentRequestDto commentRequestDto){

            Optional<User> user= userRepository.findById(commentRequestDto.getUserId());

            if(user.isEmpty()){
                ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"User not found","User not found");
                return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
            }

            Optional<Post> post= postRepository.findById(commentRequestDto.getPostId());

            if(post.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"Post not found","Post not found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
            }

            User newUser=user.get();
            Post newPost=post.get();

            Comment comment=new Comment();
            comment.setContent(commentRequestDto.getContent());
            comment.setUser(newUser);
            comment.setPost(newPost);

            commentRepository.save(comment);

           ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"comment created successfully","comment created successfully.");
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly only same user on same post allowed to update a comment.
    public ResponseEntity<ApiResponse<String>> updateComment(Long id, CommentRequestDto commentRequestDto){

        Optional<Comment> optionalComment=commentRepository.findById(id);
        Optional<User>    optionalUser=userRepository.findById(commentRequestDto.getUserId());
        Optional<Post>    optionalPost=postRepository.findById(commentRequestDto.getPostId());

        if(optionalComment.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"No comment found","No comment found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        if(optionalUser.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"No user found","No user found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        if(optionalPost.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"No post found","No post found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

       if(!Objects.equals(commentRequestDto.getUserId(), optionalComment.get().getUser().getId())){
           ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.FORBIDDEN.value(),"User not authorized to update this comment","Unauthorized");
           return new ResponseEntity<>(apiResponse,HttpStatus.FORBIDDEN);
       }

       if(!Objects.equals(commentRequestDto.getPostId(), optionalComment.get().getPost().getId())){
           ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.FORBIDDEN.value(),"Comment does not belong to the given post","Unauthorized");
           return new ResponseEntity<>(apiResponse,HttpStatus.FORBIDDEN);
       }

        Comment commentToUpdate=optionalComment.get();
        commentToUpdate.setContent(commentRequestDto.getContent());
        commentRepository.save(commentToUpdate);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"comment updated successfully","comment updated successfully.");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly deleting a comment by their comment id
    public ResponseEntity<ApiResponse<String>> deleteComment(Long id){

        Optional<Comment> optionalComment=commentRepository.findById(id);

        if(optionalComment.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"No comment found","No comment found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        commentRepository.deleteById(id);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Comment deleted successfully","Comment deleted successfully.");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
