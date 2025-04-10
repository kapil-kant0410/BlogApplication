package com.ql.BlogApplication.exception;

public class RoleNotFoundException extends CustomException {
    public RoleNotFoundException(String message){
        super(message);
    }
}
