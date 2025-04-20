package com.ql.BlogApplication.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otps")
public class Otp {

    @Id
    private String id;

    private String email;

    private String otp;

    private LocalDateTime generatedAt;
}
