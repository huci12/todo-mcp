package org.library.todo.exception

import org.springframework.http.HttpStatus

/**
 * 시스템 오류 예외 클래스들
 * 서버 내부에서 발생하는 예외들로, 주로 시스템 장애나 예기치 못한 오류
 */

/**
 * 데이터베이스 관련 예외
 * HTTP 500 Internal Server Error에 해당
 */
class DatabaseException(
    message: String,
    errorCode: String = "DATABASE_ERROR",
    cause: Throwable? = null
) : BaseException(
    message = message,
    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    errorCode = errorCode,
    isUserError = false,
    cause = cause
)

/**
 * 외부 API 호출 실패 예외
 * HTTP 502 Bad Gateway에 해당
 */
class ExternalApiException(
    message: String,
    errorCode: String = "EXTERNAL_API_ERROR",
    cause: Throwable? = null
) : BaseException(
    message = message,
    httpStatus = HttpStatus.BAD_GATEWAY,
    errorCode = errorCode,
    isUserError = false,
    cause = cause
)

/**
 * 서비스 일시적 불가능 예외
 * HTTP 503 Service Unavailable에 해당
 */
class ServiceUnavailableException(
    message: String,
    errorCode: String = "SERVICE_UNAVAILABLE",
    cause: Throwable? = null
) : BaseException(
    message = message,
    httpStatus = HttpStatus.SERVICE_UNAVAILABLE,
    errorCode = errorCode,
    isUserError = false,
    cause = cause
)

/**
 * 설정 오류 예외
 * HTTP 500 Internal Server Error에 해당
 */
class ConfigurationException(
    message: String,
    errorCode: String = "CONFIGURATION_ERROR",
    cause: Throwable? = null
) : BaseException(
    message = message,
    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    errorCode = errorCode,
    isUserError = false,
    cause = cause
)

/**
 * 일반적인 시스템 내부 오류
 * HTTP 500 Internal Server Error에 해당
 */
class InternalServerException(
    message: String,
    errorCode: String = "INTERNAL_SERVER_ERROR",
    cause: Throwable? = null
) : BaseException(
    message = message,
    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    errorCode = errorCode,
    isUserError = false,
    cause = cause
)

/**
 * 범용 시스템 오류 예외
 * HTTP 500 Internal Server Error에 해당
 * InternalServerException의 별칭으로 사용
 */
class SystemErrorException(
    message: String,
    errorCode: String = "SYSTEM_ERROR",
    cause: Throwable? = null
) : BaseException(
    message = message,
    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    errorCode = errorCode,
    isUserError = false,
    cause = cause
)

/**
 * 타임아웃 예외
 * HTTP 408 Request Timeout에 해당
 */
class TimeoutException(
    message: String,
    errorCode: String = "TIMEOUT_ERROR",
    cause: Throwable? = null
) : BaseException(
    message = message,
    httpStatus = HttpStatus.REQUEST_TIMEOUT,
    errorCode = errorCode,
    isUserError = false,
    cause = cause
)
