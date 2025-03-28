package com.ql.BlogApplication.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNotFoundException extends CustomException{
      public UserNotFoundException(String message){
             super(message);
      }
}
