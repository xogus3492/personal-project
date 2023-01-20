package com.example.board.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";
    private final long tokenValidityInMilliseconds; // 토큰 유효 시간 (밀리초)
    private final String secret;
    private Key key; // 시크릿 키

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds
    ) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    } // InitializingBean 인터페이스 메소드로, 주입 받은 secret 값을 다시 디코딩하여 key 변수에 할당 해 주기 위함 (필수 x)

    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // 권한 정보들

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    } // 토큰 생성

    /*public String createRefreshToken(String userPk) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // 이 부분 설명 필요

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    } // refresh 토큰 생성*/

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        // 토큰으로 클레임 생성

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        // 권한 정보 리스트?

        User principal = new User(claims.getSubject(), "", authorities); // 유저 객체 생성

        return new UsernamePasswordAuthenticationToken(principal, token, authorities); // Authentication 객체 생성
    } // 토큰 정보를 이용해 Athentication 객체 리턴

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); // 토큰 파싱
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}
