package org.library.todo.controller

import org.library.todo.dto.UserLoginRequest
import org.library.todo.dto.UserSignupRequest
import org.library.todo.dto.SessionUser
import org.library.todo.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid

/**
 * 사용자 관련 웹 컨트롤러
 * 회원가입, 로그인, 로그아웃 등 인증 관련 기능을 처리
 */
@Controller
class UserController(
    private val userService: UserService
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
        private const val SESSION_USER_KEY = "user"
    }
    
    /**
     * 회원가입 폼 페이지
     * GET /signup
     */
    @GetMapping("/signup")
    fun signupForm(model: Model): String {
        model.addAttribute("userSignupRequest", UserSignupRequest("", "", "", ""))
        model.addAttribute("title", "회원가입")
        model.addAttribute("hideNavigation", true) // 네비게이션 숨기기
        
        logger.info("Signup form accessed")
        return "signup"
    }
    
    /**
     * 회원가입 처리
     * POST /signup
     */
    @PostMapping("/signup")
    fun signup(
        @Valid @ModelAttribute("userSignupRequest") request: UserSignupRequest,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        try {
            // Validation 오류가 있는 경우
            if (bindingResult.hasErrors()) {
                logger.warn("Validation errors in user signup: {}", bindingResult.allErrors)
                model.addAttribute("title", "회원가입")
                model.addAttribute("hideNavigation", true) // 네비게이션 숨기기
                return "signup"
            }
            
            // 비밀번호 일치 검증
            if (!request.isPasswordMatching()) {
                model.addAttribute("error", "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
                model.addAttribute("title", "회원가입")
                model.addAttribute("hideNavigation", true) // 네비게이션 숨기기
                return "signup"
            }
            
            // 회원가입 처리
            val normalizedRequest = request.normalize()
            val userResponse = userService.signup(normalizedRequest)
            
            logger.info("User signed up successfully: email={}", userResponse.email)
            
            // 성공 메시지 추가
            redirectAttributes.addFlashAttribute("message", 
                                                "회원가입이 완료되었습니다. 로그인해주세요.")
            
            // 로그인 페이지로 리다이렉트
            return "redirect:/login"
            
        } catch (ex: Exception) {
            logger.error("Error during user signup: {}", ex.message, ex)
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다: ${ex.message}")
            model.addAttribute("title", "회원가입")
            model.addAttribute("hideNavigation", true) // 네비게이션 숨기기
            return "signup"
        }
    }
    
    /**
     * 로그인 폼 페이지
     * GET /login
     */
    @GetMapping("/login")
    fun loginForm(model: Model): String {
        model.addAttribute("userLoginRequest", UserLoginRequest("", ""))
        model.addAttribute("title", "로그인")
        model.addAttribute("hideNavigation", true) // 네비게이션 숨기기
        
        logger.info("Login form accessed")
        return "login"
    }
    
    /**
     * 로그인 처리
     * POST /login
     */
    @PostMapping("/login")
    fun login(
        @Valid @ModelAttribute("userLoginRequest") request: UserLoginRequest,
        bindingResult: BindingResult,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        try {
            // Validation 오류가 있는 경우
            if (bindingResult.hasErrors()) {
                logger.warn("Validation errors in user login: {}", bindingResult.allErrors)
                model.addAttribute("title", "로그인")
                model.addAttribute("hideNavigation", true) // 네비게이션 숨기기
                return "login"
            }
            
            // 로그인 처리
            val normalizedRequest = request.normalize()
            val userResponse = userService.login(normalizedRequest)
            
            // 세션에 사용자 정보 저장
            val sessionUser = SessionUser.from(userService.findByEmail(userResponse.email)!!)
            session.setAttribute(SESSION_USER_KEY, sessionUser)
            
            logger.info("User logged in successfully: email={}", userResponse.email)
            
            // 성공 메시지 추가
            redirectAttributes.addFlashAttribute("message", 
                                                "${userResponse.nickname}님, 환영합니다!")
            
            // 메인 페이지로 리다이렉트
            return "redirect:/"
            
        } catch (ex: Exception) {
            logger.error("Error during user login: {}", ex.message, ex)
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.")
            model.addAttribute("title", "로그인")
            model.addAttribute("hideNavigation", true) // 네비게이션 숨기기
            return "login"
        }
    }
    
    /**
     * 로그아웃 처리
     * POST /logout
     */
    @PostMapping("/logout")
    fun logout(
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        // 세션에서 사용자 정보 가져오기 (로깅용)
        val sessionUser = session.getAttribute(SESSION_USER_KEY) as? SessionUser
        
        // 세션 무효화
        session.invalidate()
        
        logger.info("User logged out successfully: email={}", sessionUser?.email ?: "unknown")
        
        // 로그아웃 메시지 추가
        redirectAttributes.addFlashAttribute("message", "로그아웃되었습니다.")
        
        // 로그인 페이지로 리다이렉트
        return "redirect:/login"
    }
    
    /**
     * 현재 로그인한 사용자 정보 조회 (유틸리티 메서드)
     */
    fun getCurrentUser(session: HttpSession): SessionUser? {
        return session.getAttribute(SESSION_USER_KEY) as? SessionUser
    }
}
