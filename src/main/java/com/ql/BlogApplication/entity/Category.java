package com.ql.BlogApplication.entity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false,unique = true,length = 50)
    private String name;

    @OneToMany(mappedBy = "category")
    @JsonManagedReference
    private List<Post> posts=new ArrayList<>();
}
