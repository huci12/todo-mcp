package org.library.todo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/**
 * 보안 설정 클래스
 * Spring Security를 비활성화하고 BCrypt만 사용
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {
    
    /**
     * BCrypt 패스워드 인코더 Bean
     * 비밀번호 암호화/검증에 사용
     */
    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }
    
    /**
     * Spring Security 비활성화
     * 세션 기반 인증을 직접 구현하므로 Spring Security는 사용하지 않음
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            
        return http.build()
    }
}
