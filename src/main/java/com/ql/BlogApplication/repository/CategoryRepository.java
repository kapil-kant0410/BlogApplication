package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
     Boolean existsByName(String name);
}
