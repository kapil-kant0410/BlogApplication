package com.ql.BlogApplication.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="comments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter

public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 500)
    private String content;

    @ManyToOne()
    @JoinColumn(name="user_id",nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne()
    @JoinColumn(name = "post_id",nullable = false)
    @JsonBackReference
    private Post post;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    Set<Like> likes=new HashSet<>();
}
