package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Otp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends MongoRepository<Otp,String> {
    Optional<Otp> findTopByEmailOrderByGeneratedAtDesc(String email);
    void deleteByGeneratedAtBefore(LocalDateTime time);
}
