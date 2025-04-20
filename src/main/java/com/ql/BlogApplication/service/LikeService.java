package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentLikeRequestDto;
import com.ql.BlogApplication.dto.PostLikeRequestDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Like;
import com.ql.BlogApplication.exception.CommentNotFoundException;
import com.ql.BlogApplication.exception.PostNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.repository.CommentRepository;
import com.ql.BlogApplication.repository.LikeRepository;
import com.ql.BlogApplication.repository.PostRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.Post;
import java.util.Optional;


@Service
public class LikeService {

   private final UserRepository userRepository;
   private final PostRepository postRepository;
   private final LikeRepository likeRepository;
   private final CommentRepository commentRepository;
   private final JwtUtil jwtUtil;

    LikeService(UserRepository userRepository,PostRepository postRepository,LikeRepository likeRepository,CommentRepository commentRepository,JwtUtil jwtUtil){
        this.userRepository=userRepository;
        this.postRepository=postRepository;
        this.likeRepository=likeRepository;
        this.commentRepository=commentRepository;
        this.jwtUtil=jwtUtil;
    }

    //working properly like and unlike a post on same endpoint
    public ResponseEntity<ApiResponse<String>> likeAPost(PostLikeRequestDto postLikeRequestDto){

        String token= TokenContext.getToken();
        String id= jwtUtil.extractId(token);

        User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
        Post post=postRepository.findById(postLikeRequestDto.getPostId()).orElseThrow(()->new PostNotFoundException(MessageCodes.messages.get(221)));

        Optional<Like> optionalLike=likeRepository.findByUserIdAndPostId(id, postLikeRequestDto.getPostId());

        if(optionalLike.isPresent()){
            likeRepository.delete(optionalLike.get());
            post.getLikeIds().remove(optionalLike.get().getId());
            user.getLikeIds().remove(optionalLike.get().getId());
            ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(152),MessageCodes.messages.get(152));
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        }

        Like likeAPost=new Like();
        likeAPost.setUserId(user.getId());
        likeAPost.setPostId(post.getId());
        likeRepository.save(likeAPost);

        post.getLikeIds().add(likeAPost.getId());
        postRepository.save(post);

        user.getLikeIds().add(likeAPost.getId());
        userRepository.save(user);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(151),MessageCodes.messages.get(151));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly like and unlike a comment on same endpoint
    public ResponseEntity<ApiResponse<String>> likeAComment(CommentLikeRequestDto commentLikeRequestDto){

        String token=TokenContext.getToken();
        String id=jwtUtil.extractId(token);

        User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
        Comment comment=commentRepository.findById(commentLikeRequestDto.getCommentId()).orElseThrow(()-> new CommentNotFoundException(MessageCodes.messages.get(241)));

        Optional<Like> optionalLike=likeRepository.findByUserIdAndCommentId(id, commentLikeRequestDto.getCommentId());

        if(optionalLike.isPresent()){
            likeRepository.delete(optionalLike.get());
            comment.getLikeIds().remove(optionalLike.get().getId());
            user.getLikeIds().remove(optionalLike.get().getId());
            ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(152),MessageCodes.messages.get(152));
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        }

        Like like=new Like();
        like.setUserId(user.getId());
        like.setCommentId(comment.getId());
        likeRepository.save(like);

        comment.getLikeIds().add(like.getId());
        commentRepository.save(comment);

        user.getLikeIds().add(like.getId());
        userRepository.save(user);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(154),MessageCodes.messages.get(154));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
