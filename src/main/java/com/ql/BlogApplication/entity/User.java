package com.ql.BlogApplication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection="users")
public class User {

    @Id
    private String id;

    private String name;

    private String email;

    private Integer tokenVersion = 0;

    @JsonIgnore
    private String password;

    // Store role IDs (references to roles)
    private Set<String> roleIds = new HashSet<>();

    // Store subscriptions (use Author IDs here)
    private Set<String> subscribedAuthorIds = new HashSet<>();

    // Store subscribers (use User IDs here)
    private Set<String> subscriberIds = new HashSet<>();

    // Store Post IDs
    private Set<String> postIds = new HashSet<>();

    // Store Comment IDs
    private Set<String> commentIds = new HashSet<>();

    // Store Like IDs
    private Set<String> likeIds = new HashSet<>();
}
