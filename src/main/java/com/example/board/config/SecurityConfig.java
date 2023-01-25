package com.example.board.config;

import com.example.board.exception.ErrorCode;
import com.example.board.exception.ErrorResponse;
import com.example.board.jwt.JwtSecurityConfig;
import com.example.board.jwt.JwtTokenProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;


@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtTokenProvider jwtTokenProvider;
    private final String[] AUTH_WHITELIST = {
            "/auth/signup",
            "/auth/signin"
    }; // 권한이 필요 없는 경로들

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 x
                //세션 설정

                .and()
                .httpBasic().disable()
                .formLogin().disable()
                // httpBasic, formLogin 설정

                .cors()
                .and()
                .csrf().disable()
                // cors, csrf 설정

                .authorizeRequests()
                .antMatchers("/auth/signup", "/auth/signin").permitAll() // 권한 x
                //.antMatchers().authenticated() // 권한 o
                .anyRequest().authenticated() // 나머지 것들 권한 필요
                // 요청 경로 권한 설정

                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())// accessDeniedHandler()를 SecurityConfig 클래스에 구현
                .authenticationEntryPoint(authenticationEntryPoint()) // authenticationEntryPoint()를 SecurityConfig 클래스에 구현
                // exception 등록

                .and()
                .apply(new JwtSecurityConfig(jwtTokenProvider));
                // jwt관련 custom 설정 클래스 적용

    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.ACCESS_DENIED);

            ObjectMapper objectMapper = new ObjectMapper(); // Obj mapper 생성

            response.getWriter()
                    .write(objectMapper.writeValueAsString(errorResponse)); // 응답 writer로 Obj mapper를 이용해 응답
        }; // 접근 거부 응답(403 response)을 받았을 때 AccessDeniedHandler클래스가 실행되어
            // 이 (request, response, accessDeniedException) 인자를 받아 내부에 저장해 리턴해 줌.
    }


    private AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED);

            ObjectMapper objectMapper = new ObjectMapper();

            response.getWriter()
                    .write(objectMapper.writeValueAsString(errorResponse));
        };
    }

}
