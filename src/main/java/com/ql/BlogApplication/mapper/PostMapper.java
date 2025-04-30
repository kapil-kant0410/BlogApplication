package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.Post;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostMapper {

    private final CommentMapper commentMapper;

    public PostMapper(CommentMapper commentMapper){
        this.commentMapper=commentMapper;
    }

    public  List<CommentResponseDto> addComments(Post post){
                 if(post.getComments()==null){
                     return new ArrayList<>();
                 }
                return post.getComments().stream().map(commentMapper::toDto).toList();
    }

    public  List<PostResponseDto> toDtoList(List<Post> allPosts){
          return  allPosts.stream().map(post -> PostResponseDto.builder()
                                                    .title(post.getTitle())
                                                    .content(post.getContent())
                                                    .commentList(addComments(post))
                                                    .build()
                                      ).toList();
    }

    public  PostResponseDto toDto(Post post){
        return  PostResponseDto.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .commentList(addComments(post))
                .build();
    }

}
