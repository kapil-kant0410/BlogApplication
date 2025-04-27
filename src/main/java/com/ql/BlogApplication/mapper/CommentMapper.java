package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapper {

     public  List<CommentResponseDto> toDtoList(List<Comment> allComments){
         return allComments.stream().map(comment -> CommentResponseDto.builder().content(comment.getContent()).name(comment.getUser().getName()).build()).toList();
     }

     public  CommentResponseDto toDto(Comment comment){
           return CommentResponseDto.builder().name((comment.getUser().getName())).content(comment.getContent()).build();
     }


}
