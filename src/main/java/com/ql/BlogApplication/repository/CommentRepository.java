package com.ql.BlogApplication.repository;
import com.ql.BlogApplication.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long> {

}
