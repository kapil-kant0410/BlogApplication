package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentLikeRequestDto;
import com.ql.BlogApplication.dto.PostLikeRequestDto;
import com.ql.BlogApplication.service.LikeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/like")
@AllArgsConstructor

public class LikeController {

      private final LikeService likeService;

      @PostMapping("/postLike")
      ResponseEntity<ApiResponse<Map<String,String>>> likeAPost(@Valid @RequestBody PostLikeRequestDto postLikeRequestDto){
            return likeService.likeAPost(postLikeRequestDto);
      }

      @PostMapping("/commentLike")
      ResponseEntity<ApiResponse<Map<String,String>>> likeAComment(@Valid @RequestBody CommentLikeRequestDto commentLikeRequestDto){
             return likeService.likeAComment(commentLikeRequestDto);
      }

}
