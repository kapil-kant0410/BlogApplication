package com.ql.BlogApplication.repository;

import com.ql.BlogApplication.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp,Long> {
    Optional<Otp> findTopByEmailAndIsUsedFalseOrderByGeneratedAtDesc(String email);
}
