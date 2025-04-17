package com.ql.BlogApplication.exception;

public class CategoryNotFoundException extends  CustomException{
   public CategoryNotFoundException(String message){
       super(message);
   }
}
