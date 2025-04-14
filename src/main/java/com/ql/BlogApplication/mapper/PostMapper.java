package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Post;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostMapper {

    public static List<CommentResponseDto> addComments(Post post){
                return post.getComments().stream().map(CommentMapper::toDto).toList();
    }

    public static List<PostResponseDto> toDtoList(List<Post> allPosts){
          return  allPosts.stream().map(post -> {
                return  PostResponseDto.builder()
                      .title(post.getTitle())
                      .content(post.getContent())
                        .commentList(addComments(post))
                      .build();
          }).toList();
    }

    public static  PostResponseDto toDto(Post post){
        return  PostResponseDto.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .commentList(addComments(post))
                .build();
    }

}
