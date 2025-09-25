package com.f1v3.reservation.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정 클래스.
 *
 * @author Seungjo, Jeong
 */
@Configuration
@EntityScan(basePackages = {"com.f1v3.reservation.common"})
@EnableJpaRepositories(basePackages = {"com.f1v3.reservation.common"})
public class JpaConfig {
}
