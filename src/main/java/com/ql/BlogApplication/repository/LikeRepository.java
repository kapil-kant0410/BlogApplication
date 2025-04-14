package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface LikeRepository extends JpaRepository<Like,Long> {

    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
    Optional<Like> findByUserIdAndCommentId(Long userId,Long commentId);

}
