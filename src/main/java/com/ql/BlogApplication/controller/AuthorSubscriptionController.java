package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.AuthorSubscribersResponseDto;
import com.ql.BlogApplication.dto.AuthorSubscriptionRequestDto;
import com.ql.BlogApplication.dto.UserSubscribedAuthorResponseDto;
import com.ql.BlogApplication.service.AuthorSubscriptionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authorSubscription")
@AllArgsConstructor

public class AuthorSubscriptionController {

    private final AuthorSubscriptionService authorSubscriptionService;

    @PostMapping("/subscribe")
    ResponseEntity<ApiResponse<String>> subscribeToAuthor(@Valid @RequestBody AuthorSubscriptionRequestDto authorSubscriptionRequestDto){
        return authorSubscriptionService.subscribeToAuthor(authorSubscriptionRequestDto);
    }

    @DeleteMapping("/unSubscribe")

    ResponseEntity<ApiResponse<String>> unsubscribeFromAuthor(@Valid @RequestBody AuthorSubscriptionRequestDto authorSubscriptionRequestDto){
        return authorSubscriptionService.unsubscribeFromAuthor(authorSubscriptionRequestDto);
    }

    @GetMapping("/allSubscriptions")
    ResponseEntity<ApiResponse<List<UserSubscribedAuthorResponseDto>>> getUserSubscriptions(){
        return authorSubscriptionService.getUserSubscriptions();
    }

    @GetMapping("/allSubscribers/{id}")
    ResponseEntity<ApiResponse<List<AuthorSubscribersResponseDto>>> getAuthorSubscribers(@Valid @PathVariable Long id){
        return authorSubscriptionService.getAuthorSubscribers(id);
    }

    @GetMapping("/subscribersCount/{id}")
    ResponseEntity<ApiResponse<Integer>> getSubscribersCount(@Valid @PathVariable Long id){
       return  authorSubscriptionService.getSubscribersCount(id);
    }



}
