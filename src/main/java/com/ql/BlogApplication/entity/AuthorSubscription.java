package com.ql.BlogApplication.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "author_subscriptions",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "author_id"}))
@Getter
@Setter
public class AuthorSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name="author_id",nullable = false)
    @JsonBackReference
    private User author;
}
