package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category,String> {
     Boolean existsByName(String name);
     Optional<Category> findByName(String category);
}
