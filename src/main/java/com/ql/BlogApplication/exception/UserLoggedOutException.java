package com.ql.BlogApplication.exception;

public class UserLoggedOutException extends  CustomException {
      public UserLoggedOutException(String message){
           super(message);
       }
}
