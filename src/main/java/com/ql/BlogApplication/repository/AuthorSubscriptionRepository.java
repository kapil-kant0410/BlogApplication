package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.AuthorSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorSubscriptionRepository extends JpaRepository<AuthorSubscription,Long> {
    Optional<AuthorSubscription> findByUserIdAndAuthorId(Long userId,Long authorId);
}
