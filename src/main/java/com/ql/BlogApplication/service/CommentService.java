package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentRequestDto;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.CommentUpdateRequestDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.CommentNotFoundException;
import com.ql.BlogApplication.exception.PostNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.mapper.CommentMapper;
import com.ql.BlogApplication.repository.CommentRepository;
import com.ql.BlogApplication.repository.PostRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;
    private final CommentMapper commentMapper;


    CommentService(CommentMapper commentMapper,CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository,JwtUtil jwtUtil){
        this.commentRepository=commentRepository;
        this.userRepository=userRepository;
        this.postRepository=postRepository;
        this.jwtUtil=jwtUtil;
        this.commentMapper=commentMapper;
    }

    //working properly creating a comment
    public ResponseEntity<ApiResponse<Map<String,CommentResponseDto>>> createComment(CommentRequestDto commentRequestDto){

            String token= TokenContext.getToken();
            Long userId= Long.parseLong(jwtUtil.extractId(token));

            User user= userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
            Post post= postRepository.findById(commentRequestDto.getPostId()).orElseThrow(()->new PostNotFoundException(MessageCodes.messages.get(221)));

            if(post.getIsPublished()==Boolean.FALSE){
                ApiResponse<Map<String,CommentResponseDto>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(),null,"Cant comment on unpublished post");
                return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
            }

            Comment comment=new Comment();
            comment.setContent(commentRequestDto.getContent());
            comment.setUser(user);
            comment.setPost(post);

            commentRepository.save(comment);

            CommentResponseDto commentResponseDto=commentMapper.toDto(comment);
            Map<String,CommentResponseDto> data=new HashMap<>();
            data.put("Created Comment",commentResponseDto);

           ApiResponse<Map<String,CommentResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,MessageCodes.messages.get(141));
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly only same user on same post allowed to update a comment.
    public ResponseEntity<ApiResponse<Map<String,CommentResponseDto>>> updateComment(Long id, CommentUpdateRequestDto commentUpdateRequestDto){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        Comment comment=commentRepository.findByUserIdAndId(userId,id).orElseThrow(()->new CommentNotFoundException(MessageCodes.messages.get(241)));

        comment.setContent(commentUpdateRequestDto.getContent());
        commentRepository.save(comment);

        CommentResponseDto commentResponseDto=commentMapper.toDto(comment);
        Map<String,CommentResponseDto> data=new HashMap<>();
        data.put("Updated Comment",commentResponseDto);

        ApiResponse<Map<String,CommentResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,MessageCodes.messages.get(142));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly deleting a comment by their comment id
    public ResponseEntity<ApiResponse<Map<String,CommentResponseDto>>> deleteComment(Long commentId){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        Comment comment=commentRepository.findByUserIdAndId(userId,commentId).orElseThrow(()->new CommentNotFoundException(MessageCodes.messages.get(241)));
        commentRepository.deleteById(commentId);

        CommentResponseDto commentResponseDto=commentMapper.toDto(comment);
        Map<String,CommentResponseDto> data=new HashMap<>();
        data.put("Deleted comment",commentResponseDto);

        ApiResponse<Map<String,CommentResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,MessageCodes.messages.get(143));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
