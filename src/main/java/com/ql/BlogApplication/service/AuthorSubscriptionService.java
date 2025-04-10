package com.ql.BlogApplication.service;

import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.AuthorSubscriptionRequestDto;
import com.ql.BlogApplication.dto.UserSubscribedAuthorResponseDto;
import com.ql.BlogApplication.dto.AuthorSubscribersResponseDto;
import com.ql.BlogApplication.entity.AuthorSubscription;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.UserRole;
import com.ql.BlogApplication.repository.AuthorSubscriptionRepository;
import com.ql.BlogApplication.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;



import java.util.*;

@Service
public class AuthorSubscriptionService {

    UserRepository userRepository;
    AuthorSubscriptionRepository authorSubscriptionRepository;

    AuthorSubscriptionService(UserRepository userRepository,AuthorSubscriptionRepository authorSubscriptionRepository){
        this.userRepository=userRepository;
        this.authorSubscriptionRepository=authorSubscriptionRepository;
    }

    //working properly subscribe to an author
    public ResponseEntity<ApiResponse<String>> subscribeToAuthor(AuthorSubscriptionRequestDto authorSubscriptionRequestDto){

        Optional<User> userOptional=userRepository.findById(authorSubscriptionRequestDto.getUserId());
        Optional<User> authorOptional=userRepository.findById(authorSubscriptionRequestDto.getAuthorId());

        if(userOptional.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "User not found","User not found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        if(authorOptional.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Author not found","Author not found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        if(Objects.equals(userOptional.get().getId(), authorOptional.get().getId())){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid Operation","A user cannot subscribe to themselves.");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Optional<AuthorSubscription> authorSubscriptionOptional=authorSubscriptionRepository.findByUserIdAndAuthorId(authorSubscriptionRequestDto.getUserId(), authorSubscriptionRequestDto.getAuthorId());

        if(authorSubscriptionOptional.isPresent()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Already subscribed","Already subscribed to author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        User author=authorOptional.get();
        Set<UserRole> authorRoles=author.getUserRoles();

        if(authorRoles.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Not an author","This user is not assigned any roles");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        boolean isAuthor=authorRoles.stream().anyMatch(userRole -> "author".equalsIgnoreCase(userRole.getRole().getName()));

        if(!isAuthor){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus. FORBIDDEN.value(), "Not an author","Not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.FORBIDDEN);
        }

        AuthorSubscription authorSubscription=new AuthorSubscription();
        authorSubscription.setUser(userOptional.get());
        authorSubscription.setAuthor(authorOptional.get());

        authorSubscriptionRepository.save(authorSubscription);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), "Successfully subscribed to author","Successfully subscribed to author");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly to unsubscribe an author
    @Transactional
    public ResponseEntity<ApiResponse<String>> unsubscribeFromAuthor(AuthorSubscriptionRequestDto authorSubscriptionRequestDto){

        Optional<User> optionalUser=userRepository.findById(authorSubscriptionRequestDto.getUserId());
        Optional<User> optionalAuthor=userRepository.findById(authorSubscriptionRequestDto.getAuthorId());

        if(optionalUser.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "User not found","User not found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        if(optionalAuthor.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Author not found","Author not found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        Optional<AuthorSubscription> optionalAuthorSubscription=authorSubscriptionRepository.findByUserIdAndAuthorId(authorSubscriptionRequestDto.getUserId(), authorSubscriptionRequestDto.getAuthorId());

        if(optionalAuthorSubscription.isEmpty()){
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No relation between user and author","No relation");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        authorSubscriptionRepository.delete(optionalAuthorSubscription.get());

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), "Successfully unsubscribe from author","Successfully unsubscribe");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly getting user subscribed authors
    public ResponseEntity<ApiResponse<List<UserSubscribedAuthorResponseDto>>> getUserSubscriptions(Long id){

        Optional<User> optionalUser=userRepository.findById(id);

        if(optionalUser.isEmpty()){
            ApiResponse<List<UserSubscribedAuthorResponseDto>> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), Collections.emptyList(),"User not found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        User user=optionalUser.get();
        Set<AuthorSubscription> subscribedAuthors=user.getSubscribedAuthors();

        List<UserSubscribedAuthorResponseDto> authorSubscriptionResponseDtoList= subscribedAuthors.stream().map(authorSubscription -> {
           User author=authorSubscription.getAuthor();
           return UserSubscribedAuthorResponseDto.builder()
                   .authorId(author.getId())
                   .name(author.getName())
                   .email(author.getEmail())
                   .build();
           }).toList();


        ApiResponse<List<UserSubscribedAuthorResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),authorSubscriptionResponseDtoList,"Successfully found all authors");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly getting author subscribers
    public ResponseEntity<ApiResponse<List<AuthorSubscribersResponseDto>>> getAuthorSubscribers(Long id){

        Optional<User> optionalUser=userRepository.findById(id);

        if(optionalUser.isEmpty()){
            ApiResponse<List<AuthorSubscribersResponseDto>> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), Collections.emptyList(),"User not found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        boolean isAuthor = optionalUser.get().getUserRoles().stream()
                .anyMatch(userRole -> "author".equalsIgnoreCase(userRole.getRole().getName()));

        if(!isAuthor){
            ApiResponse<List<AuthorSubscribersResponseDto>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), Collections.emptyList(),"Provide user is not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<AuthorSubscription> authorSubscribers= optionalUser.get().getSubscribers();

       List<AuthorSubscribersResponseDto> authorSubscribersList= authorSubscribers.stream().map(subscriber->{
            User user=subscriber.getUser();
           return AuthorSubscribersResponseDto.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .build();
        }).toList();

        ApiResponse<List<AuthorSubscribersResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), authorSubscribersList,"All subscribers list");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    //working properly return count of subscribed users for an author
    public ResponseEntity<ApiResponse<Integer>> getSubscribersCount(Long id){

        Optional<User> optionalUser=userRepository.findById(id);

        if(optionalUser.isEmpty()){
            ApiResponse<Integer> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), 0,"User not found");
            return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
        }

        boolean isAuthor = optionalUser.get().getUserRoles().stream()
                .anyMatch(userRole -> "author".equalsIgnoreCase(userRole.getRole().getName()));

        if(!isAuthor){
            ApiResponse<Integer> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), 0,"Provided user is not an author");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        Set<AuthorSubscription> authorSubscribers= optionalUser.get().getSubscribers();

        ApiResponse<Integer> apiResponse=ApiResponse.success(HttpStatus.OK.value(), authorSubscribers.size(),"Successfully fetched count");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
