package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentRequestDto;
import com.ql.BlogApplication.dto.CommentUpdateRequestDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.CommentNotFoundException;
import com.ql.BlogApplication.exception.PostNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.repository.CommentRepository;
import com.ql.BlogApplication.repository.PostRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;


    CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository,JwtUtil jwtUtil){
        this.commentRepository=commentRepository;
        this.userRepository=userRepository;
        this.postRepository=postRepository;
        this.jwtUtil=jwtUtil;
    }

    //working properly creating a comment
    public ResponseEntity<ApiResponse<String>> createComment(CommentRequestDto commentRequestDto){

            String token= TokenContext.getToken();
            Long id= Long.parseLong(jwtUtil.extractId(token));

            User user= userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
            Post post= postRepository.findById(commentRequestDto.getPostId()).orElseThrow(()->new PostNotFoundException(MessageCodes.messages.get(221)));

            if(post.getIsPublished()==Boolean.FALSE){
                ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.CONFLICT.value(),"Cant comment on unpublished post","Cant comment on unpublished post");
                return new ResponseEntity<>(apiResponse,HttpStatus.CONFLICT);
            }

            Comment comment=new Comment();
            comment.setContent(commentRequestDto.getContent());
            comment.setUser(user);
            comment.setPost(post);

            commentRepository.save(comment);

           ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(141),MessageCodes.messages.get(141));
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly only same user on same post allowed to update a comment.
    public ResponseEntity<ApiResponse<String>> updateComment(Long id, CommentUpdateRequestDto commentUpdateRequestDto){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        Comment comment=commentRepository.findByUserIdAndId(userId,id).orElseThrow(()->new CommentNotFoundException(MessageCodes.messages.get(241)));

        comment.setContent(commentUpdateRequestDto.getContent());
        commentRepository.save(comment);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(142),MessageCodes.messages.get(142));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly deleting a comment by their comment id
    public ResponseEntity<ApiResponse<String>> deleteComment(Long id){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        commentRepository.findByUserIdAndId(userId,id).orElseThrow(()->new CommentNotFoundException(MessageCodes.messages.get(241)));
        commentRepository.deleteById(id);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(143),MessageCodes.messages.get(143));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
