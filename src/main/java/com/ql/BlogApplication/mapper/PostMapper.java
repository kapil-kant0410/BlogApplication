package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Post;
import com.ql.BlogApplication.exception.CommentNotFoundException;
import com.ql.BlogApplication.repository.CommentRepository;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PostMapper {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    PostMapper(CommentMapper commentMapper,CommentRepository commentRepository){
        this.commentRepository=commentRepository;
        this.commentMapper=commentMapper;
    }

    public List<CommentResponseDto> addComments(Post post){
           return post.getCommentIds().stream().map(commentId->{
               Comment comment=commentRepository.findById(commentId).orElseThrow(()->new CommentNotFoundException(MessageCodes.messages.get(241)));
               return  commentMapper.toDto(comment);
           }).toList();
    }

    public List<PostResponseDto> toDtoList(List<Post> allPosts){
          return  allPosts.stream().map(post -> (
                                PostResponseDto.builder()
                               .title(post.getTitle())
                               .content(post.getContent())
                               .commentList(addComments(post))
                               .build()
          )).toList();
    }

    public PostResponseDto toDto(Post post){
        return  PostResponseDto.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .commentList(addComments(post))
                .build();
    }

}
