package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Like;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface LikeRepository extends MongoRepository<Like,String> {
    Optional<Like> findByUserIdAndPostId(String userId, String postId);
    Optional<Like> findByUserIdAndCommentId(String userId,String commentId);
}
