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

    JwtUtil(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    public String generateToken(Long id) {
        User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(106)));
        return Jwts.builder()
                .setSubject(id.toString())
                .claim("tokenVersion",user.getTokenVersion())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            Long id=Long.parseLong(extractId(token));
            User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(106)));

            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            Integer tokenVersion=claims.get("tokenVersion",Integer.class);

            if(!Objects.equals(tokenVersion, user.getTokenVersion())){
                throw new UserLoggedOutException("Logged out user.");
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
             logger.error("error while validating the token",e);
              return false;
        }
    }

    public String extractId(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
