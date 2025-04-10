package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {
    List<Post> findByIsPublishedTrue();
}
