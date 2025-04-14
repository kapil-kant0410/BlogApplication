package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.entity.Comment;
import java.util.List;

public class CommentMapper {

     public static List<CommentResponseDto> toDtoList(List<Comment> allComments){

         return allComments.stream().map(comment -> {
               return CommentResponseDto.builder()
                       .content(comment.getContent())
                       .name(comment.getUser().getName())
                       .id(comment.getId())
                       .build();
         }).toList();

     }

     public static CommentResponseDto toDto(Comment comment){
           return CommentResponseDto.builder()
                   .name((comment.getUser().getName()))
                   .id(comment.getId())
                 .content(comment.getContent())
                 .build();
     }


}
