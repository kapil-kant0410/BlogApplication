package com.ql.BlogApplication.exception;

import com.ql.BlogApplication.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger=  LoggerFactory.getLogger(GlobalExceptionHandler.class);

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse> handleGlobalExceptions(Exception ex){
//        Map<String,String> errors=new HashMap<>();
//        errors.put("error",ex.getMessage());
//        ApiResponse exceptionResponse=new ApiResponse(false,HttpStatus.BAD_REQUEST.value(), errors,"throw by global exception");
//        return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFoundException(UserNotFoundException ex){
        logger.error("An error occurred: {}", ex.getMessage());
        Map<String,String> errors=new HashMap<>();
        errors.put("error",ex.getMessage());
        ApiResponse exceptionResponse=new ApiResponse( false,HttpStatus.NOT_FOUND.value(),errors , "User not found with particular id");
         return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponse> handlePostNotFoundException(PostNotFoundException ex){
           logger.error("An error occurred: {}", ex.getMessage());
           Map<String,String> errors=new HashMap<>();
           errors.put("error",ex.getMessage());
           ApiResponse exceptionResponse=new ApiResponse(false,HttpStatus.NOT_FOUND.value(), errors, "Post not found with particular id");
           return new ResponseEntity<>(exceptionResponse,HttpStatus.NOT_FOUND);
    }

    //the requested URL does not match any existing endpoint (controller method).
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse> handleHandlerNotFoundException(NoHandlerFoundException ex){
        logger.error("An error occurred: {}", ex.getMessage());
        Map<String,String> errors=new HashMap<>();
        errors.put("error",ex.getMessage());
        ApiResponse exceptionResponse=new ApiResponse(false,HttpStatus.NOT_FOUND.value(),errors,"No handler found for your request. Please check the API endpoint.");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.NOT_FOUND);
    }

    //throw exception when mismatch in datatypes in request body or request body errors. example passing string inside int datatype.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
        logger.error("An error occurred: {}", ex.getMessage());
        Map<String,String> errors=new HashMap<>();
        errors.put("error",ex.getMessage());
        ApiResponse exceptionResponse=new ApiResponse(false,HttpStatus.BAD_REQUEST.value(), errors,"Malformed request body. Please check request body format.");
          return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    //throw exception if body fields does not fulfill the constraints in case of @validate
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(ConstraintViolationException ex){
           logger.error("An error occurred: {}", ex.getMessage());
           Map<String,String> errors=new HashMap<>();
           errors.put("error",ex.getMessage());
           ApiResponse exceptionResponse=new ApiResponse(false,HttpStatus.BAD_REQUEST.value(), errors,"Validation failed. Check the provided data.");
           return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    //throw exception if body fields does not fulfill the constraints in case of @valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("An error occurred: {}", ex.getMessage());
        Map<String,String> errors=new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        ApiResponse exceptionResponse=new ApiResponse(false,HttpStatus.BAD_REQUEST.value(),errors,"Validation failed. Check the provided data.");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    //throw exception when its required to use post/get/delete/put, but we are using wrong method.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex){
          logger.error("An error occurred: {}", ex.getMessage());
          Map<String,String> errors=new HashMap<>();
          errors.put("error",ex.getMessage());
          ApiResponse exceptionResponse=new ApiResponse(false,HttpStatus.METHOD_NOT_ALLOWED.value(), errors,"Please provide valid request method eg. get for get not post");
          return new ResponseEntity<>(exceptionResponse,HttpStatus.METHOD_NOT_ALLOWED);
    }

}
