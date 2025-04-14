package com.ql.BlogApplication.repository;
import com.ql.BlogApplication.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long> {
   Optional<Comment> findByUserIdAndId(Long userId,Long id);
}
