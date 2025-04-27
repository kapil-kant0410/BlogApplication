package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentRequestDto;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.CommentUpdateRequestDto;
import com.ql.BlogApplication.entity.Comment;
import com.ql.BlogApplication.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/create")
    ResponseEntity<ApiResponse<Map<String, CommentResponseDto>>> createComment(@Valid @RequestBody CommentRequestDto commentRequestDto){
         return commentService.createComment(commentRequestDto);
    }

    @PutMapping("/update/{id}")
    ResponseEntity<ApiResponse<Map<String,CommentResponseDto>>> updateComment(@PathVariable Long id, @Valid @RequestBody CommentUpdateRequestDto commentUpdateRequestDto){
        return commentService.updateComment(id,commentUpdateRequestDto);
    }

    @DeleteMapping("/delete/{id}")
    ResponseEntity<ApiResponse<Map<String, CommentResponseDto>>> deleteComment(@PathVariable Long id){
        return commentService.deleteComment(id);
    }

}
