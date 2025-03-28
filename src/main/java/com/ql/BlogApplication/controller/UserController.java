package com.ql.BlogApplication.controller;
import com.ql.BlogApplication.dto.UserRequest;
import com.ql.BlogApplication.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/users")

public class UserController {

     @PostMapping("/allData/{id}")
     public String getAllData(@PathVariable Long id,@RequestBody @Valid UserRequest userRequest){
           //throw new PostNotFoundException("hello from post not found exception");
          //throw new UserNotFoundException("User not found with this particular id");
            return userRequest.getId()+" "+ userRequest.getAge()+" "+userRequest.getMessage();
     }

}
