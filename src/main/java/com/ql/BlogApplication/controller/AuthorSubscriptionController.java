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
import java.util.Map;

@RestController
@RequestMapping("/api/authorSubscription")
@AllArgsConstructor

public class AuthorSubscriptionController {

    private final AuthorSubscriptionService authorSubscriptionService;

    @PostMapping("/subscribe")
    ResponseEntity<ApiResponse<Map<String,String>>> subscribeToAuthor(@Valid @RequestBody AuthorSubscriptionRequestDto authorSubscriptionRequestDto){
        return authorSubscriptionService.subscribeToAuthor(authorSubscriptionRequestDto);
    }

    @DeleteMapping("/un-subscribe")

    ResponseEntity<ApiResponse<Map<String,String>>> unsubscribeFromAuthor(@Valid @RequestBody AuthorSubscriptionRequestDto authorSubscriptionRequestDto){
        return authorSubscriptionService.unsubscribeFromAuthor(authorSubscriptionRequestDto);
    }

    @GetMapping("/all-subscriptions")
    ResponseEntity<ApiResponse<Map<String,List<UserSubscribedAuthorResponseDto>>>> getUserSubscriptions(){
        return authorSubscriptionService.getUserSubscriptions();
    }

    @GetMapping("/all-subscribers/{id}")
    ResponseEntity<ApiResponse< Map<String,List<AuthorSubscribersResponseDto>> >> getAuthorSubscribers(@Valid @PathVariable Long id){
        return authorSubscriptionService.getAuthorSubscribers(id);
    }

    @GetMapping("/subscribers-count/{id}")
    ResponseEntity<ApiResponse<Map<String,Integer>>> getSubscribersCount(@Valid @PathVariable Long id){
       return  authorSubscriptionService.getSubscribersCount(id);
    }



}
