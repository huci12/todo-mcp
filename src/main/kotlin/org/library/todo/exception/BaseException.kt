package org.library.todo.exception

import org.springframework.http.HttpStatus

/**
 * 모든 사용자 정의 예외의 기본 클래스
 *
 * @property message 예외 메시지
 * @property httpStatus HTTP 상태 코드
 * @property errorCode 내부 에러 코드
 * @property isUserError 사용자 오류 여부 (true: 사용자 오류, false: 시스템 오류)
 */
abstract class BaseException(
    override val message: String,
    val httpStatus: HttpStatus,
    val errorCode: String,
    val isUserError: Boolean,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    /**
     * 예외 정보를 맵으로 변환
     * 로깅 및 응답 생성에 사용
     * 하위 클래스에서 오버라이드 가능
     */
    open fun toMap(): Map<String, Any> {
        return mapOf(
            "message" to message,
            "httpStatus" to httpStatus.value(),
            "errorCode" to errorCode,
            "isUserError" to isUserError,
            "timestamp" to System.currentTimeMillis()
        )
    }
}
