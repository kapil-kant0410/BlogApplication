package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.repository.UserRepository;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CommentMapper {

    private  final UserRepository userRepository ;

    public CommentMapper(UserRepository userRepository){
        this.userRepository=userRepository;
    }

     public List<CommentResponseDto> toDtoList(List<Comment> allComments){
         return allComments.stream().map(comment -> {
             User user=userRepository.findById(comment.getUserId()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
               return CommentResponseDto.builder()
                       .content(comment.getContent())
                       .name(user.getName())
                       .id(comment.getId())
                       .build();
         }).toList();
     }

     public  CommentResponseDto toDto(Comment comment){
         User user=userRepository.findById(comment.getUserId()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
           return CommentResponseDto.builder()
                   .name(user.getName())
                   .id(comment.getId())
                   .content(comment.getContent())
                   .build();
     }


}
