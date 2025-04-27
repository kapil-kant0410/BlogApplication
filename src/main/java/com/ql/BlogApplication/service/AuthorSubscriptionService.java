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
    public ResponseEntity<ApiResponse<Map<String,String>>> subscribeToAuthor(AuthorSubscriptionRequestDto authorSubscriptionRequestDto){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        User user=userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
        User author=userRepository.findById(authorSubscriptionRequestDto.getAuthorId()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        if(Objects.equals(user.getId(), author.getId())){
            ApiResponse<Map<String,String>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), null,"A user cannot subscribe to themselves.");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Optional<AuthorSubscription> authorSubscriptionOptional=authorSubscriptionRepository.findByUserIdAndAuthorId(userId, authorSubscriptionRequestDto.getAuthorId());

        if(authorSubscriptionOptional.isPresent()){
            ApiResponse<Map<String,String>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), null,"Already subscribed to this author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<UserRole> authorRoles=author.getUserRoles();

        boolean isAuthor=authorRoles.stream().anyMatch(userRole -> "author".equalsIgnoreCase(userRole.getRole().getName()));

        if(!isAuthor){
            ApiResponse<Map<String,String>> apiResponse=ApiResponse.error(HttpStatus. FORBIDDEN.value(), null,"Author id not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.FORBIDDEN);
        }

        AuthorSubscription authorSubscription=new AuthorSubscription();
        authorSubscription.setUser(user);
        authorSubscription.setAuthor(author);

        authorSubscriptionRepository.save(authorSubscription);

        ApiResponse<Map<String,String>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),Collections.emptyMap(),MessageCodes.messages.get(161));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly to unsubscribe an author
    @Transactional
    public ResponseEntity<ApiResponse<Map<String,String>>> unsubscribeFromAuthor(AuthorSubscriptionRequestDto authorSubscriptionRequestDto){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        userRepository.findById(authorSubscriptionRequestDto.getAuthorId()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        Optional<AuthorSubscription> optionalAuthorSubscription=authorSubscriptionRepository.findByUserIdAndAuthorId(userId, authorSubscriptionRequestDto.getAuthorId());

        if(optionalAuthorSubscription.isEmpty()){
            ApiResponse<Map<String,String>> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), null,"No relation between user and author");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        authorSubscriptionRepository.delete(optionalAuthorSubscription.get());

        ApiResponse<Map<String,String>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), Collections.emptyMap(),MessageCodes.messages.get(162));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly getting user subscribed authors
    public ResponseEntity<ApiResponse<Map<String,List<UserSubscribedAuthorResponseDto>>>> getUserSubscriptions(){

        String token= TokenContext.getToken();
        Long id= Long.parseLong(jwtUtil.extractId(token));

        User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        Set<AuthorSubscription> subscribedAuthors=user.getSubscribedAuthors();

        List<UserSubscribedAuthorResponseDto> authorSubscriptionResponseDtoList= SubscriptionsMapper.toDtoList(subscribedAuthors);

        Map<String,List<UserSubscribedAuthorResponseDto>> data=new HashMap<>();
        data.put("User/Author all subscribed authors",authorSubscriptionResponseDtoList);

        ApiResponse< Map<String,List<UserSubscribedAuthorResponseDto>> > apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,MessageCodes.messages.get(163));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly getting author subscribers
    public ResponseEntity<ApiResponse< Map<String,List<AuthorSubscribersResponseDto>>  >> getAuthorSubscribers(Long id){

        User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        boolean isAuthor = user.getUserRoles().stream()
                .anyMatch(userRole -> "author".equalsIgnoreCase(userRole.getRole().getName()));

        if(!isAuthor){
            ApiResponse<Map<String,List<AuthorSubscribersResponseDto>>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), null,"Provide user is not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<AuthorSubscription> authorSubscribers= user.getSubscribers();
        List<AuthorSubscribersResponseDto> authorSubscribersList= SubscriberMapper.toDtoList(authorSubscribers);

        Map<String,List<AuthorSubscribersResponseDto>> data=new HashMap<>();
        data.put("Author all subscribers",authorSubscribersList);

        ApiResponse<Map<String,List<AuthorSubscribersResponseDto>>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), data,MessageCodes.messages.get(164));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly return count of subscribed users for an author
    public ResponseEntity<ApiResponse<Map<String,Integer>>> getSubscribersCount(Long id){

        User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));

        boolean isAuthor = user.getUserRoles().stream()
                .anyMatch(userRole -> "author".equalsIgnoreCase(userRole.getRole().getName()));

        if(!isAuthor){
            ApiResponse<Map<String,Integer>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), null,"Provided user is not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<AuthorSubscription> authorSubscribers= user.getSubscribers();
        Map<String,Integer> data=new HashMap<>();
        data.put("All subscribers count",authorSubscribers.size());

        ApiResponse<Map<String,Integer>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), data,"All subscribers count");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
