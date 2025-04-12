package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {
    List<Post> findByIsPublishedTrue();
    Optional<Post> findByAuthorIdAndId(Long userId, Long postId);
}
