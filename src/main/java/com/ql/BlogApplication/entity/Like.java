package com.ql.BlogApplication.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "likes")
public class Like {

    @Id
    private String id;

    private String userId;

    private String postId;     // Nullable: if like is for a post

    private String commentId;  // Nullable: if like is for a comment

    @Transient
    public void validateLikeAssociation() {
        if ((postId != null && commentId != null) || (postId == null && commentId == null)) {
            throw new IllegalArgumentException("A like must be associated with either a post or a comment, but not both.");
        }
    }
}
