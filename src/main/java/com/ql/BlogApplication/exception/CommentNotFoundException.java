package com.ql.BlogApplication.exception;

public class CommentNotFoundException extends CustomException{
    public CommentNotFoundException(String message){
        super(message);
    }
}
