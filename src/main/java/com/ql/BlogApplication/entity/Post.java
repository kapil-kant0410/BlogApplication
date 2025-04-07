package com.ql.BlogApplication.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Entity
@Table(name = "posts",uniqueConstraints = @UniqueConstraint(columnNames = {"title","content","author_id","category_id"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Post {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "author_id",nullable = false)
    private User author;

    @ManyToOne
    @JoinColumn(name="category_id",nullable = false)
    @JsonBackReference
    private Category category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<Like> likes;
}
