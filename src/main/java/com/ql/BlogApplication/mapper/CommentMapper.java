package com.ql.BlogApplication.mapper;
import com.ql.BlogApplication.dto.CommentRequestDto;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.entity.Comment;
import java.util.List;

public class CommentMapper {

     public static List<CommentResponseDto> toDtoList(List<Comment> allComments){

         return allComments.stream().map(comment -> {
               return CommentResponseDto.builder()
                       .content(comment.getContent())
                       .build();
         }).toList();

     }

     public static CommentResponseDto toDto(Comment comment){
           return CommentResponseDto.builder()
                 .content(comment.getContent())
                 .build();
     }


}
