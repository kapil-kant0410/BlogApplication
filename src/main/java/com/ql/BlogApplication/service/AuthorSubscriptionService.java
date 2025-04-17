package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.AuthorSubscriptionRequestDto;
import com.ql.BlogApplication.dto.UserSubscribedAuthorResponseDto;
import com.ql.BlogApplication.dto.AuthorSubscribersResponseDto;
import com.ql.BlogApplication.entity.AuthorSubscription;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.mapper.SubscriberMapper;
import com.ql.BlogApplication.mapper.SubscriptionsMapper;
import com.ql.BlogApplication.repository.AuthorSubscriptionRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class AuthorSubscriptionService {

    UserRepository userRepository;
    AuthorSubscriptionRepository authorSubscriptionRepository;
    JwtUtil jwtUtil;

    AuthorSubscriptionService(UserRepository userRepository,AuthorSubscriptionRepository authorSubscriptionRepository,JwtUtil jwtUtil){
        this.userRepository=userRepository;
        this.authorSubscriptionRepository=authorSubscriptionRepository;
        this.jwtUtil=jwtUtil;
    }

    //working properly subscribe to an author
    public ResponseEntity<ApiResponse<String>> subscribeToAuthor(AuthorSubscriptionRequestDto authorSubscriptionRequestDto){

        String token= TokenContext.getToken();
        Long id= Long.parseLong(jwtUtil.extractId(token));

        User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
        User author=userRepository.findById(authorSubscriptionRequestDto.getAuthorId()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));


        if(Objects.equals(user.getId(), author.getId())){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid Operation","A user cannot subscribe to themselves.");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Optional<AuthorSubscription> authorSubscriptionOptional=authorSubscriptionRepository.findByUserIdAndAuthorId(id, authorSubscriptionRequestDto.getAuthorId());

        if(authorSubscriptionOptional.isPresent()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Already subscribed","Already subscribed to author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<UserRole> authorRoles=author.getUserRoles();

        boolean isAuthor=authorRoles.stream().anyMatch(userRole -> "author".equalsIgnoreCase(userRole.getRole().getName()));

        if(!isAuthor){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus. FORBIDDEN.value(), "Not an author","Not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.FORBIDDEN);
        }

        AuthorSubscription authorSubscription=new AuthorSubscription();
        authorSubscription.setUser(user);
        authorSubscription.setAuthor(author);

        authorSubscriptionRepository.save(authorSubscription);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(161),MessageCodes.messages.get(161));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly to unsubscribe an author
    @Transactional
    public ResponseEntity<ApiResponse<String>> unsubscribeFromAuthor(AuthorSubscriptionRequestDto authorSubscriptionRequestDto){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        userRepository.findById(authorSubscriptionRequestDto.getAuthorId()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        Optional<AuthorSubscription> optionalAuthorSubscription=authorSubscriptionRepository.findByUserIdAndAuthorId(userId, authorSubscriptionRequestDto.getAuthorId());

        if(optionalAuthorSubscription.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No relation between user and author","No relation");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        authorSubscriptionRepository.delete(optionalAuthorSubscription.get());

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(162),MessageCodes.messages.get(162));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly getting user subscribed authors
    public ResponseEntity<ApiResponse<List<UserSubscribedAuthorResponseDto>>> getUserSubscriptions(){

        String token= TokenContext.getToken();
        Long id= Long.parseLong(jwtUtil.extractId(token));

        User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        Set<AuthorSubscription> subscribedAuthors=user.getSubscribedAuthors();

        List<UserSubscribedAuthorResponseDto> authorSubscriptionResponseDtoList= SubscriptionsMapper.toDtoList(subscribedAuthors);

        ApiResponse<List<UserSubscribedAuthorResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),authorSubscriptionResponseDtoList,MessageCodes.messages.get(163));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly getting author subscribers
    public ResponseEntity<ApiResponse<List<AuthorSubscribersResponseDto>>> getAuthorSubscribers(Long id){

        User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        boolean isAuthor = user.getUserRoles().stream()
                .anyMatch(userRole -> "author".equalsIgnoreCase(userRole.getRole().getName()));

        if(!isAuthor){
            ApiResponse<List<AuthorSubscribersResponseDto>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), Collections.emptyList(),"Provide user is not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<AuthorSubscription> authorSubscribers= user.getSubscribers();
        List<AuthorSubscribersResponseDto> authorSubscribersList= SubscriberMapper.toDtoList(authorSubscribers);

        ApiResponse<List<AuthorSubscribersResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), authorSubscribersList,MessageCodes.messages.get(164));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly return count of subscribed users for an author
    public ResponseEntity<ApiResponse<Integer>> getSubscribersCount(Long id){

        User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));

        boolean isAuthor = user.getUserRoles().stream()
                .anyMatch(userRole -> "author".equalsIgnoreCase(userRole.getRole().getName()));

        if(!isAuthor){
            ApiResponse<Integer> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), 0,"Provided user is not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<AuthorSubscription> authorSubscribers= user.getSubscribers();

        ApiResponse<Integer> apiResponse=ApiResponse.success(HttpStatus.OK.value(), authorSubscribers.size(),"All subscribers count");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
