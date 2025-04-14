package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.Post;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostMapper {

    public static List<PostResponseDto> toDtoList(List<Post> allPosts){
          return  allPosts.stream().map(post -> {
                return  PostResponseDto.builder()
                      .title(post.getTitle())
                      .content(post.getContent())
                      .build();
          }).toList();
    }

    public static  PostResponseDto toDto(Post post){
        return  PostResponseDto.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .build();
    }

}
