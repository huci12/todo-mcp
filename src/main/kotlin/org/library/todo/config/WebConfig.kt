package org.library.todo.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC 설정 클래스
 * 인터셉터 등록 및 기타 웹 관련 설정
 */
@Configuration
class WebConfig(
    private val loginInterceptor: LoginInterceptor
) : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/**") // 모든 경로에 적용
            .excludePathPatterns(
                "/login",           // 로그인 페이지
                "/signup",          // 회원가입 페이지
                "/css/**",          // CSS 파일
                "/js/**",           // JavaScript 파일
                "/images/**",       // 이미지 파일
                "/favicon.ico",     // 파비콘
                "/error",           // 에러 페이지
                "/h2-console/**"    // H2 콘솔 (개발용)
            )
    }
}
