package com.ql.BlogApplication.service;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentLikeRequestDto;
import com.ql.BlogApplication.dto.PostLikeRequestDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.entity.Like;
import com.ql.BlogApplication.repository.CommentRepository;
import com.ql.BlogApplication.repository.LikeRepository;
import com.ql.BlogApplication.repository.PostRepository;
import com.ql.BlogApplication.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.Post;

import java.util.Optional;

@Service
public class LikeService {

    UserRepository userRepository;
    PostRepository postRepository;
    LikeRepository likeRepository;
    CommentRepository commentRepository;

    LikeService(UserRepository userRepository,PostRepository postRepository,LikeRepository likeRepository,CommentRepository commentRepository){
        this.userRepository=userRepository;
        this.postRepository=postRepository;
        this.likeRepository=likeRepository;
        this.commentRepository=commentRepository;
    }

    //working properly like and unlike a post on same endpoint
    public ResponseEntity<ApiResponse<String>> likeAPost(PostLikeRequestDto postLikeRequestDto){

        Optional<User>  optionalUser=userRepository.findById(postLikeRequestDto.getUserId());
        Optional<Post> optionalPost=postRepository.findById(postLikeRequestDto.getPostId());

        if(optionalUser.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No user found","No user found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        if(optionalPost.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No post found","No post found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        Optional<Like> optionalLike=likeRepository.findByUserIdAndPostId(postLikeRequestDto.getUserId(), postLikeRequestDto.getPostId());

        if(optionalLike.isPresent()){
            likeRepository.delete(optionalLike.get());
            ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), "Unliked the post","Successfully removed like");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        }

        Like likeAPost=new Like();
        likeAPost.setUser(optionalUser.get());
        likeAPost.setPost(optionalPost.get());

        likeRepository.save(likeAPost);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), "Successfully like the post","Successfully like the post");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly like and unlike a comment on same endpoint
    public ResponseEntity<ApiResponse<String>> likeAComment(CommentLikeRequestDto commentLikeRequestDto){

        Optional<User> optionalUser=userRepository.findById(commentLikeRequestDto.getUserId());
        Optional<Comment> optionalComment=commentRepository.findById(commentLikeRequestDto.getCommentId());

        if(optionalUser.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No user found","No user found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        if(optionalComment.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No comment found","No comment found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        Optional<Like> optionalLike=likeRepository.findByUserIdAndCommentId(commentLikeRequestDto.getUserId(), commentLikeRequestDto.getCommentId());

        if(optionalLike.isPresent()){
            likeRepository.delete(optionalLike.get());
            ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), "Unliked the comment","Successfully removed like");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        }

        Like like=new Like();
        like.setUser(optionalUser.get());
        like.setComment(optionalComment.get());

        likeRepository.save(like);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), "Successfully like the comment","Successfully like the comment");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }


}
