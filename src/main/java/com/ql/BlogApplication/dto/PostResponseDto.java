package com.ql.BlogApplication.dto;

import com.ql.BlogApplication.entity.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class PostResponseDto {
   private  String title;
   private  String content;
   List<CommentResponseDto> commentList=new ArrayList<>();
}
