package org.library.todo.exception

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * API 에러 응답 DTO
 * 클라이언트에게 반환되는 에러 정보를 담는 데이터 클래스
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    /**
     * 에러 메시지
     */
    val message: String,
    
    /**
     * HTTP 상태 코드
     */
    val status: Int,
    
    /**
     * 내부 에러 코드
     */
    val errorCode: String,
    
    /**
     * 에러 발생 시간
     */
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    /**
     * 요청 경로 (옵셔널)
     */
    val path: String? = null,
    
    /**
     * 필드별 유효성 검증 에러 (옵셔널)
     */
    val fieldErrors: Map<String, String>? = null,
    
    /**
     * 추가 상세 정보 (옵셔널)
     */
    val details: Map<String, Any>? = null
) {
    companion object {
        /**
         * BaseException으로부터 ErrorResponse 생성
         */
        fun from(exception: BaseException, path: String? = null): ErrorResponse {
            return ErrorResponse(
                message = exception.message,
                status = exception.httpStatus.value(),
                errorCode = exception.errorCode,
                path = path,
                fieldErrors = if (exception is ValidationException) exception.fieldErrors else null
            )
        }
        
        /**
         * 일반 Exception으로부터 ErrorResponse 생성 (시스템 오류)
         */
        fun fromGenericException(
            exception: Exception, 
            status: Int = 500, 
            errorCode: String = "INTERNAL_SERVER_ERROR",
            path: String? = null
        ): ErrorResponse {
            return ErrorResponse(
                message = "An unexpected error occurred",
                status = status,
                errorCode = errorCode,
                path = path,
                details = mapOf("originalMessage" to (exception.message ?: "Unknown error"))
            )
        }
    }
}
