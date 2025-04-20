package com.ql.BlogApplication.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    private String title;

    private String content;

    private Boolean isPublished = false;

    private String imageURL;

    // Reference to User (Author)
    private String authorId;

    // Reference to Category
    private String categoryId;

    // Store comment IDs instead of embedding full documents
    private Set<String> commentIds = new HashSet<>();

    // Store like IDs instead of embedding full documents
    private Set<String> likeIds = new HashSet<>();
}

