package com.ql.BlogApplication.util;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.exception.UserLoggedOutException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.repository.UserRepository;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    UserRepository userRepository;
    private final String tokenVersion="tokenVersion";
    private final String tokenType="tokenType";

    JwtUtil(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.accessTokenExpiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;


    public String generateAccessToken(Long id) {
        User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
        return Jwts.builder()
                .setSubject(id.toString())
                .claim(tokenVersion,user.getTokenVersion())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateRefreshToken(Long id) {
        User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
        return Jwts.builder()
                .setSubject(id.toString())
                .claim(tokenVersion,user.getTokenVersion())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }


    public boolean validateToken(String token) {
        try {

            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

            Long id=Long.parseLong(extractId(token));
            User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));

            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            Integer tokenVersionFromToken=claims.get(tokenVersion,Integer.class);

            if(!Objects.equals(tokenVersionFromToken, user.getTokenVersion())){
                throw new UserLoggedOutException("Logged out user.");
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
             logger.error("error while validating the token",e);
              return false;
        }
    }

    public String extractId(String token) {

       if (token == null || token.trim().isEmpty()) {
         throw new IllegalArgumentException("JWT token is null or empty in extractId()");
       }

      Claims claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();

      return claims.getSubject();

    }

}
