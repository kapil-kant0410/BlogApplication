package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CommentRepository extends MongoRepository<Comment,String> {
   Optional<Comment> findByUserIdAndId(String userId,String id);
}
