package com.ql.BlogApplication.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="likes",uniqueConstraints = {
      @UniqueConstraint(columnNames = {"user_id","post_id"}),
      @UniqueConstraint(columnNames = {"user_id","comment_id"})
})

public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne()
    @JoinColumn(name="post_id",nullable = true)
    @JsonBackReference
    private Post post;

    @ManyToOne()
    @JoinColumn(name = "comment_id",nullable = true)
    @JsonBackReference
    private Comment comment;

    @PrePersist
    @PreUpdate
    private  void validateLikeAssociation(){
          if((post!=null&&comment!=null)||(post==null&&comment==null)){
                 throw new IllegalArgumentException("A like must be associated with either a post or a comment, but not both.");
          }
    }

}

