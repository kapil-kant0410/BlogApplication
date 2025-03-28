package com.ql.BlogApplication.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
@Getter
@Setter

public class ApiResponse<T> {
    private boolean status;
    private int code;
    private String message;
    private T data;
    private Map<String,String> errors;


    public ApiResponse(boolean status, int code, Map<String,String> errors, String message){
         this.status=status;
         this.code=code;
         this.errors=errors;
         this.message=message;
         this.data= (T) Collections.emptyMap();
    }

    public ApiResponse(boolean status, int code, T data, String message){
        this.status=status;
        this.code=code;
        this.data=data;
        this.message=message;
        this.errors= Collections.emptyMap();
    }

}
