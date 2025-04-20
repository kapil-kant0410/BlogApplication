package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends MongoRepository<Post,String> {
    List<Post> findByIsPublishedTrue();
    List<Post> findByCategoryIdAndIsPublishedTrue(String categoryId);
    Optional<Post> findByAuthorIdAndId(String userId, String postId);
    List<Post> findByAuthorId(String authorId);
}
