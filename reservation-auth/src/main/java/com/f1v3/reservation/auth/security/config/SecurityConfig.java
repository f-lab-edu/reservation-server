package com.f1v3.reservation.auth.security.config;

import com.f1v3.reservation.auth.security.FailedAuthenticationEntryPoint;
import com.f1v3.reservation.auth.token.filter.TokenAuthenticationFilter;
import com.f1v3.reservation.auth.token.filter.TokenExceptionFilter;
import com.f1v3.reservation.common.domain.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static com.f1v3.reservation.auth.token.TokenConstants.PUBLIC_URLS;

/**
 * Spring Security 설정 클래스
 *
 * @author Seungjo, Jeong
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final TokenExceptionFilter tokenExceptionFilter;
    private final FailedAuthenticationEntryPoint failedAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers("/v1/admin/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers("/v1/supplier/**").hasRole(UserRole.SUPPLIER.name())
                        .requestMatchers("/v1/**").hasRole(UserRole.USER.name())
                        .anyRequest().authenticated()
                )
                .cors(cors -> cors.configurationSource(getCorsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handler -> handler.authenticationEntryPoint(failedAuthenticationEntryPoint))
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenExceptionFilter, TokenAuthenticationFilter.class)
                .build();
    }

    private CorsConfigurationSource getCorsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(List.of("*")); // fixme: domain 설정 필요
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            config.setAllowCredentials(false);
            config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Access-Control-Allow-Headers", "Access-Control-Expose-Headers"));
            config.setExposedHeaders(List.of("Authorization"));
            return config;
        };
    }
}
