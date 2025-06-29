package org.library.todo.dto

import org.library.todo.entity.User
import jakarta.validation.constraints.*

/**
 * 회원가입 요청 DTO
 */
data class UserSignupRequest(
    /**
     * 이메일 (로그인 ID)
     * 필수 입력, 이메일 형식 검증
     */
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다")
    @field:Email(message = "올바른 이메일 형식을 입력해주세요")
    @field:Size(max = 100, message = "이메일은 100자 이하로 입력해주세요")
    val email: String,
    
    /**
     * 비밀번호
     * 필수 입력, 최소 6자 이상
     */
    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다")
    @field:Size(min = 6, max = 50, message = "비밀번호는 6자 이상 50자 이하로 입력해주세요")
    val password: String,
    
    /**
     * 비밀번호 확인
     * 필수 입력, password와 일치해야 함
     */
    @field:NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다")
    val passwordConfirm: String,
    
    /**
     * 닉네임
     * 필수 입력, 2자 이상 20자 이하
     */
    @field:NotBlank(message = "닉네임은 필수 입력 항목입니다")
    @field:Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요")
    @field:Pattern(
        regexp = "^[가-힣a-zA-Z0-9_-]+$",
        message = "닉네임은 한글, 영문, 숫자, _, - 만 사용 가능합니다"
    )
    val nickname: String
) {
    /**
     * 비밀번호 일치 여부 검증
     */
    fun isPasswordMatching(): Boolean {
        return password == passwordConfirm
    }
    
    /**
     * 입력값 정규화
     */
    fun normalize(): UserSignupRequest {
        return this.copy(
            email = this.email.trim().lowercase(),
            nickname = this.nickname.trim()
        )
    }
}

/**
 * 로그인 요청 DTO
 */
data class UserLoginRequest(
    /**
     * 이메일 (로그인 ID)
     */
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다")
    @field:Email(message = "올바른 이메일 형식을 입력해주세요")
    val email: String,
    
    /**
     * 비밀번호
     */
    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다")
    val password: String
) {
    /**
     * 입력값 정규화
     */
    fun normalize(): UserLoginRequest {
        return this.copy(
            email = this.email.trim().lowercase()
        )
    }
}

/**
 * 사용자 응답 DTO (세션 저장용)
 */
data class UserResponse(
    /**
     * 사용자 ID
     */
    val id: Long,
    
    /**
     * 이메일
     */
    val email: String,
    
    /**
     * 닉네임
     */
    val nickname: String
) {
    companion object {
        /**
         * User Entity를 UserResponse DTO로 변환
         */
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                email = user.email,
                nickname = user.nickname
            )
        }
    }
}

/**
 * 세션에 저장될 사용자 정보
 * 직렬화 가능하도록 설계
 */
data class SessionUser(
    val id: Long,
    val email: String,
    val nickname: String
) {
    companion object {
        /**
         * User Entity를 SessionUser로 변환
         */
        fun from(user: User): SessionUser {
            return SessionUser(
                id = user.id,
                email = user.email,
                nickname = user.nickname
            )
        }
    }
}
