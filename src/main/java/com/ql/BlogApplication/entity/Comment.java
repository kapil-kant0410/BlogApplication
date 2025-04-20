package com.ql.BlogApplication.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "comments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Comment {

    @Id
    private String id;

    private String content;

    // Store userId instead of a User object
    private String userId;

    // Store postId instead of a Post object
    private String postId;

    // List of like IDs for this comment
    private Set<String> likeIds = new HashSet<>();
}
