package com.ql.BlogApplication.constant;

import java.util.HashMap;
import java.util.Map;

public class MessageCodes {

    public static final Map<Integer,String> messages=new HashMap<>();

    static{
        messages.put(101,"User registered successfully");
        messages.put(102,"User logged in successfully");
        messages.put(103,"User updated successfully");
        messages.put(104,"User deleted successfully");
        messages.put(105,"User fetched successfully");
        messages.put(106,"User not found");



        messages.put(107,"Role not found");
        messages.put(108,"Role assigned successfully");


        messages.put(109,"Post created successfully");
        messages.put(110,"Post updated successfully");
        messages.put(111,"Post deleted successfully");
        messages.put(112,"Post not found");
        messages.put(113,"Post fetched successfully");


    }


}
