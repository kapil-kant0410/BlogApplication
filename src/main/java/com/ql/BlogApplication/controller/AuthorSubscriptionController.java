package com.ql.BlogApplication.controller;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.AuthorSubscriptionRequestDto;
import com.ql.BlogApplication.entity.User;
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

    @DeleteMapping("/unsubscribe")

    ResponseEntity<ApiResponse<String>> unsubscribeFromAuthor(@Valid @RequestBody AuthorSubscriptionRequestDto authorSubscriptionRequestDto){
        return authorSubscriptionService.unsubscribeFromAuthor(authorSubscriptionRequestDto);
    }

    @GetMapping("/all-subscriptions")
    ResponseEntity<ApiResponse<List<User>>> getUserSubscriptions(){
        return authorSubscriptionService.getUserSubscriptions();
    }

    @GetMapping("/all-subscribers")
    ResponseEntity<ApiResponse<List<User>>> getAuthorSubscribers(){
        return authorSubscriptionService.getAuthorSubscribers();
    }

    @GetMapping("/subscribers-count")
    ResponseEntity<ApiResponse<Integer>> getSubscribersCount(){
       return  authorSubscriptionService.getSubscribersCount();
    }


}
