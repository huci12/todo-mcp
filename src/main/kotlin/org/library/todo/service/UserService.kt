package org.library.todo.service

import org.library.todo.dto.*
import org.library.todo.entity.User
import org.library.todo.exception.*
import org.library.todo.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 서비스 클래스
 * 사용자 관련 비즈니스 로직을 처리
 */
@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
    }
    
    /**
     * 회원가입 처리
     */
    @Transactional
    fun signup(request: UserSignupRequest): UserResponse {
        try {
            // 입력값 정규화
            val normalizedRequest = request.normalize()
            
            // 비밀번호 일치 검증
            if (!normalizedRequest.isPasswordMatching()) {
                throw PasswordMismatchException()
            }
            
            // 이메일 중복 검사
            if (userRepository.existsByEmail(normalizedRequest.email)) {
                throw DuplicateEmailException(normalizedRequest.email)
            }
            
            // 닉네임 중복 검사 (선택사항)
            if (userRepository.existsByNickname(normalizedRequest.nickname)) {
                throw InvalidRequestException(
                    message = "Nickname already exists: ${normalizedRequest.nickname}",
                    errorCode = "DUPLICATE_NICKNAME"
                )
            }
            
            // 비밀번호 암호화
            val encodedPassword = passwordEncoder.encode(normalizedRequest.password)
            
            // 사용자 생성
            val user = User(
                email = normalizedRequest.email,
                password = encodedPassword,
                nickname = normalizedRequest.nickname
            )
            
            // 저장
            val savedUser = userRepository.save(user)
            
            logger.info("User created successfully: email={}, nickname={}", 
                       savedUser.email, savedUser.nickname)
            
            return UserResponse.from(savedUser)
            
        } catch (ex: BaseException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("Failed to create user: {}", ex.message, ex)
            throw SystemErrorException(
                message = "Failed to create user",
                errorCode = "USER_CREATE_FAILED",
                cause = ex
            )
        }
    }
    
    /**
     * 로그인 처리
     */
    fun login(request: UserLoginRequest): UserResponse {
        try {
            // 입력값 정규화
            val normalizedRequest = request.normalize()
            
            // 사용자 조회
            val user = userRepository.findByEmail(normalizedRequest.email)
                ?: throw InvalidCredentialsException()
            
            // 비밀번호 검증
            if (!passwordEncoder.matches(normalizedRequest.password, user.password)) {
                throw InvalidCredentialsException()
            }
            
            logger.info("User logged in successfully: email={}", user.email)
            
            return UserResponse.from(user)
            
        } catch (ex: BaseException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("Failed to login user: {}", ex.message, ex)
            throw SystemErrorException(
                message = "Login failed",
                errorCode = "LOGIN_FAILED",
                cause = ex
            )
        }
    }
    
    /**
     * 사용자 ID로 조회
     */
    fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow {
            UserNotFoundException("User with id: $id")
        }
    }
    
    /**
     * 이메일로 사용자 조회
     */
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    /**
     * 이메일 중복 검사
     */
    fun isEmailExists(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }
    
    /**
     * 닉네임 중복 검사
     */
    fun isNicknameExists(nickname: String): Boolean {
        return userRepository.existsByNickname(nickname)
    }
}
