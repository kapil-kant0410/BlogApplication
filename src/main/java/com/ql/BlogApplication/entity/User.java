package com.ql.BlogApplication.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")


public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private Integer tokenVersion=0;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<UserRole> userRoles=new HashSet<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    Set<AuthorSubscription> subscribedAuthors=new HashSet<>();

    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    Set<AuthorSubscription> subscribers=new HashSet<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Post> posts=new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> comments=new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<Like> likes=new ArrayList<>();
}
