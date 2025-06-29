package org.library.todo.exception

import org.springframework.http.HttpStatus

/**
 * 사용자 오류 예외 클래스들
 * 클라이언트에서 잘못된 요청을 보낸 경우 발생하는 예외들
 */

/**
 * 리소스를 찾을 수 없는 경우 발생하는 예외
 * HTTP 404 Not Found에 해당
 */
open class ResourceNotFoundException(
    message: String,
    errorCode: String = "RESOURCE_NOT_FOUND"
) : BaseException(
    message = message,
    httpStatus = HttpStatus.NOT_FOUND,
    errorCode = errorCode,
    isUserError = true
)

/**
 * 잘못된 요청 데이터로 인한 예외
 * HTTP 400 Bad Request에 해당
 */
open class InvalidRequestException(
    message: String,
    errorCode: String = "INVALID_REQUEST",
    cause: Throwable? = null
) : BaseException(
    message = message,
    httpStatus = HttpStatus.BAD_REQUEST,
    errorCode = errorCode,
    isUserError = true,
    cause = cause
)

/**
 * 유효성 검증 실패 예외
 * HTTP 400 Bad Request에 해당
 */
open class ValidationException(
    message: String,
    val fieldErrors: Map<String, String> = emptyMap(),
    errorCode: String = "VALIDATION_FAILED"
) : BaseException(
    message = message,
    httpStatus = HttpStatus.BAD_REQUEST,
    errorCode = errorCode,
    isUserError = true
) {

    override fun toMap(): Map<String, Any> {
        val baseMap = super.toMap().toMutableMap()
        if (fieldErrors.isNotEmpty()) {
            baseMap["fieldErrors"] = fieldErrors
        }
        return baseMap
    }
}

/**
 * 리소스 중복 예외
 * HTTP 409 Conflict에 해당
 */
class DuplicateResourceException(
    message: String,
    errorCode: String = "DUPLICATE_RESOURCE"
) : BaseException(
    message = message,
    httpStatus = HttpStatus.CONFLICT,
    errorCode = errorCode,
    isUserError = true
)

/**
 * 권한 부족 예외
 * HTTP 403 Forbidden에 해당
 */
class AccessDeniedException(
    message: String = "Access denied",
    errorCode: String = "ACCESS_DENIED"
) : BaseException(
    message = message,
    httpStatus = HttpStatus.FORBIDDEN,
    errorCode = errorCode,
    isUserError = true
)

/**
 * 인증 실패 예외
 * HTTP 401 Unauthorized에 해당
 */
class AuthenticationException(
    message: String = "Authentication failed",
    errorCode: String = "AUTHENTICATION_FAILED"
) : BaseException(
    message = message,
    httpStatus = HttpStatus.UNAUTHORIZED,
    errorCode = errorCode,
    isUserError = true
)
