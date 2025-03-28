package com.ql.BlogApplication.exception;

public class PostNotFoundException extends CustomException {
            public PostNotFoundException(String message){
                super(message);
            }
}
