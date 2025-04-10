package com.ql.BlogApplication.service;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentRequestDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.CommentNotFoundException;
import com.ql.BlogApplication.exception.PostNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.interceptor.AuthorInterceptor;
import com.ql.BlogApplication.repository.CommentRepository;
import com.ql.BlogApplication.repository.PostRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.util.JwtUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AuthorInterceptor authorInterceptor;
    private final HttpServletRequest httpServletRequest;
    private final JwtUtil jwtUtil;

    CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository, AuthorInterceptor authorInterceptor, HttpServletRequest httpServletRequest,JwtUtil jwtUtil){
        this.commentRepository=commentRepository;
        this.userRepository=userRepository;
        this.postRepository=postRepository;
        this.authorInterceptor=authorInterceptor;
        this.httpServletRequest=httpServletRequest;
        this.jwtUtil=jwtUtil;
    }

    //working properly creating a comment
    public ResponseEntity<ApiResponse<String>> createComment(CommentRequestDto commentRequestDto){

            String token= authorInterceptor.getToken(httpServletRequest);
            Long id= Long.parseLong(jwtUtil.extractId(token));

            User user= userRepository.findById(id).orElseThrow(()->new UserNotFoundException("User not found"));
            Post post= postRepository.findById(commentRequestDto.getPostId()).orElseThrow(()->new PostNotFoundException("Post not found"));

            if(post.getIsPublished()==Boolean.FALSE){
                ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.CONFLICT.value(),"Cant comment on unpublished post","Cant comment on unpublished post");
                return new ResponseEntity<>(apiResponse,HttpStatus.CONFLICT);
            }

            Comment comment=new Comment();
            comment.setContent(commentRequestDto.getContent());
            comment.setUser(user);
            comment.setPost(post);

            commentRepository.save(comment);

           ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"comment created successfully","comment created successfully.");
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly only same user on same post allowed to update a comment.
    public ResponseEntity<ApiResponse<String>> updateComment(Long id, CommentRequestDto commentRequestDto){

        String token= authorInterceptor.getToken(httpServletRequest);
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        postRepository.findById(commentRequestDto.getPostId()).orElseThrow(()->new PostNotFoundException("Post not found"));

        Comment comment=commentRepository.findById(id).orElseThrow(()->new CommentNotFoundException("Comment not found"));

       if(!Objects.equals(userId, comment.getUser().getId())){
           ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.FORBIDDEN.value(),"User not authorized to update this comment","Unauthorized");
           return new ResponseEntity<>(apiResponse,HttpStatus.FORBIDDEN);
       }

       if(!Objects.equals(commentRequestDto.getPostId(), comment.getPost().getId())){
           ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(),"Comment does not belong to the given post","Unauthorized");
           return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
       }

        comment.setContent(commentRequestDto.getContent());
        commentRepository.save(comment);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"comment updated successfully","comment updated successfully.");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly deleting a comment by their comment id
    public ResponseEntity<ApiResponse<String>> deleteComment(Long id){

        String token= authorInterceptor.getToken(httpServletRequest);
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        Comment comment=commentRepository.findById(id).orElseThrow(()->new CommentNotFoundException("Comment not found"));

        if(comment.getUser().getId()!=userId){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(),"Not allowed","Not allowed");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        commentRepository.deleteById(id);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Comment deleted successfully","Comment deleted successfully.");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
