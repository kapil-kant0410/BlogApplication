package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.AuthorSubscribersResponseDto;
import com.ql.BlogApplication.dto.AuthorSubscriptionRequestDto;
import com.ql.BlogApplication.dto.UserSubscribedAuthorResponseDto;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.mapper.SubscriberMapper;
import com.ql.BlogApplication.mapper.SubscriptionsMapper;
import com.ql.BlogApplication.repository.RoleRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class AuthorSubscriptionService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    JwtUtil jwtUtil;

    AuthorSubscriptionService(UserRepository userRepository,JwtUtil jwtUtil,RoleRepository roleRepository){
        this.userRepository=userRepository;
        this.jwtUtil=jwtUtil;
        this.roleRepository=roleRepository;
    }

    //working properly subscribe to an author
    public ResponseEntity<ApiResponse<String>> subscribeToAuthor(AuthorSubscriptionRequestDto authorSubscriptionRequestDto){

        String token= TokenContext.getToken();
        String id=jwtUtil.extractId(token);

        User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
        User author=userRepository.findById(authorSubscriptionRequestDto.getAuthorId()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));


        if(Objects.equals(user.getId(), author.getId())){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid Operation","A user cannot subscribe to themselves.");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<String> authorIds= user.getSubscribedAuthorIds();
        boolean alreadySubscribed= authorIds.contains(author.getId());

        if(alreadySubscribed){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Already subscribed","Already subscribed to author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<String> authorRoleIds=author.getRoleIds();
        List<Role> authorRoles=roleRepository.findAllById(authorRoleIds);
        boolean isAuthor=authorRoles.stream().anyMatch(role -> "author".equalsIgnoreCase(role.getName()));

        if(!isAuthor){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus. FORBIDDEN.value(), "Not an author","Not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.FORBIDDEN);
        }

        user.getSubscribedAuthorIds().add(author.getId());
        author.getSubscriberIds().add(user.getId());
        userRepository.save(user);
        userRepository.save(author);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(161),MessageCodes.messages.get(161));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly to unsubscribe an author
    public ResponseEntity<ApiResponse<String>> unsubscribeFromAuthor(AuthorSubscriptionRequestDto authorSubscriptionRequestDto){

        String token= TokenContext.getToken();
        String userId= jwtUtil.extractId(token);

        User user= userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));
        User author=userRepository.findById(authorSubscriptionRequestDto.getAuthorId()).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        Set<String> subscribedAuthorIds= user.getSubscribedAuthorIds();
        boolean hasAuthor=subscribedAuthorIds.contains(authorSubscriptionRequestDto.getAuthorId());

        if(!hasAuthor){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No relation between user and author","No relation");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        user.getSubscribedAuthorIds().remove(author.getId());
        author.getSubscriberIds().remove(user.getId());

        userRepository.save(author);
        userRepository.save(user);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), MessageCodes.messages.get(162),MessageCodes.messages.get(162));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly getting user subscribed authors
    public ResponseEntity<ApiResponse<List<UserSubscribedAuthorResponseDto>>> getUserSubscriptions(){

        String token= TokenContext.getToken();
        String id= jwtUtil.extractId(token);

        User user=userRepository.findById(id).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        Set<String> subscribedAuthorIds=user.getSubscribedAuthorIds();
        List<User> subscribedAuthors=userRepository.findAllById(subscribedAuthorIds);

        List<UserSubscribedAuthorResponseDto> authorSubscriptionResponseDtoList= SubscriptionsMapper.toDtoList(subscribedAuthors);

        ApiResponse<List<UserSubscribedAuthorResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),authorSubscriptionResponseDtoList,MessageCodes.messages.get(163));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly getting author subscribers
    public ResponseEntity<ApiResponse<List<AuthorSubscribersResponseDto>>> getAuthorSubscribers(){

        String token= TokenContext.getToken();
        String userId= jwtUtil.extractId(token);
        User user=userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(MessageCodes.messages.get(201)));

        Set<String> subscriberIds=user.getSubscriberIds();
        Set<String> roleIds=user.getRoleIds();
        List<Role> roles=roleRepository.findAllById(roleIds);

        boolean isAuthor = roles.stream()
                .anyMatch(role -> "author".equalsIgnoreCase(role.getName()));

        if(!isAuthor){
            ApiResponse<List<AuthorSubscribersResponseDto>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), Collections.emptyList(),"Provide user is not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

          List<User> authorSubscribers= userRepository.findAllById(subscriberIds);
          List<AuthorSubscribersResponseDto> authorSubscribersList= SubscriberMapper.toDtoList(authorSubscribers);

        ApiResponse<List<AuthorSubscribersResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), authorSubscribersList,MessageCodes.messages.get(164));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly return count of subscribed users for an author
    public ResponseEntity<ApiResponse<Integer>> getSubscribersCount(){

        String token= TokenContext.getToken();
        String userId= jwtUtil.extractId(token);
        User user=userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));

        Set<String> roleIds=user.getRoleIds();
        List<Role> userRoles=roleRepository.findAllById(roleIds);


        boolean isAuthor = userRoles.stream()
                .anyMatch(role -> "author".equalsIgnoreCase(role.getName()));

        if(!isAuthor){
            ApiResponse<Integer> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), 0,"Provided user is not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<String> authorSubscribers= user.getSubscriberIds();

        ApiResponse<Integer> apiResponse=ApiResponse.success(HttpStatus.OK.value(), authorSubscribers.size(),"All subscribers count");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
