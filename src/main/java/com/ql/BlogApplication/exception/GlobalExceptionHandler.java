package com.ql.BlogApplication.exception;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import io.jsonwebtoken.security.SignatureException;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final  Logger logger=  LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public  ResponseEntity<ApiResponse<String>> handleAllExceptions(Exception ex){
        logger.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"An unexpected error occurred.", "Something went wrong on the server.");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFoundException(UserNotFoundException ex){
        logger.warn("User not found: {}", ex.getMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(),MessageCodes.messages.get(201));
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserLoggedOutException.class)
    public ResponseEntity<ApiResponse<String>> handleUserLoggedOutException(UserLoggedOutException userLoggedOutException){
        logger.warn("Logged out user: {}", userLoggedOutException.getMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.UNAUTHORIZED.value(),"User already logged out","Logged out user");
        return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleRoleNotFoundException(RoleNotFoundException ex){
        logger.warn("Role not found: {}", ex.getMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(),MessageCodes.messages.get(211));
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handlePostNotFoundException(PostNotFoundException ex){
           logger.warn("Post not found: {}", ex.getMessage());
           ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(),MessageCodes.messages.get(221));
           return new ResponseEntity<>(exceptionResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleCommentNotFoundException(CommentNotFoundException ex){
        logger.warn("Comment not found: {}", ex.getMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(),MessageCodes.messages.get(241));
        return new ResponseEntity<>(exceptionResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public  ResponseEntity<ApiResponse<String>> handleCategoryNotFoundException(CategoryNotFoundException categoryNotFound){
        logger.warn("category not found: {}", categoryNotFound.getMessage());
        ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(), MessageCodes.messages.get(231),MessageCodes.messages.get(231));
        return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
    }

    //the requested URL does not match any existing endpoint (controller method).
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNoResourceFoundException(NoResourceFoundException ex){
        logger.warn("No handler found for request: {}", ex.getMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.NOT_FOUND.value(),"No endpoint found for the requested URL.","Please check the URL and try again.");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.NOT_FOUND);
    }

    //throw exception when mismatch in datatypes in request body or request body errors. example passing string inside int datatype.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
        logger.error("Malformed request body: {}", ex.getMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid request format","Invalid request format.");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    //throw exception if body fields does not fulfill the constraints in case of @validate
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(ConstraintViolationException ex){
           ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(),"Validation failed. Check the provided data.");
           return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    //throw exception if body fields does not fulfill the constraints in case of @valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String,String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        Map<String,String> errors=new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );
        ApiResponse<Map<String,String>> exceptionResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(),errors,"Validation failed. Please check the provided data.");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    //throw exception when its required to use post/get/delete/put, but we are using wrong method.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex){
          logger.error("Invalid HTTP method used: {}", ex.getMethod());
          String message = "Invalid HTTP method: " + ex.getMethod() + ". Allowed methods: " + ex.getSupportedHttpMethods();
          ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), message,"Please use the correct HTTP method as per API documentation.");
          return new ResponseEntity<>(exceptionResponse,HttpStatus.METHOD_NOT_ALLOWED);
    }

    //throw exception when required to pass int in path variable but passing string.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex){
        logger.error("Type mismatch error: Expected {} but got {} message {}", ex.getRequiredType(), ex.getValue(),ex.getMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(),"Ensure that all request parameters have the correct data types.","Ensure that all request parameters have the correct data types.");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex){
        logger.error("Database integrity violation: {}", ex.getMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.CONFLICT.value(),"Database constraint violation occurred.","Please ensure data uniqueness and integrity.");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<String>> handleExpiredJwtException(ExpiredJwtException expiredJwtException){
        logger.error("Token is expired {}",expiredJwtException.getLocalizedMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.UNAUTHORIZED.value(),"Token expired","Token expired");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiResponse<String>> handleSignatureException(SignatureException signatureException){
        logger.error("Token signature not valid {}",signatureException.getLocalizedMessage());
        ApiResponse<String> exceptionResponse=ApiResponse.error(HttpStatus.UNAUTHORIZED.value(),"Token signature not valid","Token signature not valid");
        return new ResponseEntity<>(exceptionResponse,HttpStatus.UNAUTHORIZED);
    }

}
