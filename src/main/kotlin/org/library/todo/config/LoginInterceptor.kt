package org.library.todo.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * 로그인 인터셉터
 * 로그인이 필요한 페이지에 대한 접근을 제어
 */
@Component
class LoginInterceptor : HandlerInterceptor {
    
    companion object {
        private val logger = LoggerFactory.getLogger(LoginInterceptor::class.java)
        private const val SESSION_USER_KEY = "user"
    }
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val requestURI = request.requestURI
        val session = request.session
        
        // 세션에서 사용자 정보 확인
        val user = session.getAttribute(SESSION_USER_KEY)
        
        if (user == null) {
            logger.info("Unauthorized access attempt to: {} from IP: {}", 
                       requestURI, request.remoteAddr)
            
            // 로그인 페이지로 리다이렉트
            response.sendRedirect("/login")
            return false
        }
        
        return true
    }
}
