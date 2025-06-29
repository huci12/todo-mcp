package org.library.todo.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

/**
 * 전역 예외 처리 핸들러
 * 애플리케이션에서 발생하는 모든 예외를 처리하고 적절한 HTTP 응답으로 변환
 * 
 * @RestControllerAdvice: 모든 컨트롤러에서 발생하는 예외를 처리
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    /**
     * 사용자 정의 예외 처리
     * BaseException을 상속받은 모든 예외 처리
     */
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(
        exception: BaseException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        
        val errorResponse = ErrorResponse.from(exception, request.requestURI)
        
        // 로깅 처리
        logException(exception, request, errorResponse)
        
        return ResponseEntity
            .status(exception.httpStatus)
            .body(errorResponse)
    }

    /**
     * Spring Boot Validation 예외 처리
     * @Valid 어노테이션으로 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        
        // 필드 에러 정보 추출
        val fieldErrors = mutableMapOf<String, String>()
        
        // 필드 레벨 에러 처리
        exception.bindingResult.fieldErrors.forEach { error ->
            fieldErrors[error.field] = error.defaultMessage ?: "Invalid value"
        }
        
        // 글로벌 레벨 에러 처리 (객체 레벨 validation)
        exception.bindingResult.globalErrors.forEach { error ->
            fieldErrors[error.objectName] = error.defaultMessage ?: "Object validation failed"
        }
        
        val validationException = ValidationException(
            message = "입력값 검증에 실패했습니다",
            fieldErrors = fieldErrors
        )
        
        val errorResponse = ErrorResponse.from(validationException, request.requestURI)
        
        // 사용자 오류로 INFO 레벨 로깅
        logger.info("Validation error occurred - Path: {}, Fields: {}", 
            request.requestURI, fieldErrors.keys)
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    /**
     * Bean Validation 제약 조건 위반 예외 처리
     * @Validated 어노테이션으로 검증 실패 시 발생 (주로 Service/Controller 메서드 파라미터)
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        exception: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        
        // 제약 조건 위반 정보 추출
        val fieldErrors = exception.constraintViolations.associate { violation ->
            // 속성 경로에서 마지막 요소를 필드명으로 사용
            val fieldName = violation.propertyPath.toString().split(".").lastOrNull() ?: "unknown"
            fieldName to violation.message
        }
        
        val validationException = ValidationException(
            message = "제약 조건 검증에 실패했습니다",
            fieldErrors = fieldErrors
        )
        
        val errorResponse = ErrorResponse.from(validationException, request.requestURI)
        
        // 사용자 오류로 INFO 레벨 로깅
        logger.info("Constraint violation occurred - Path: {}, Violations: {}", 
            request.requestURI, fieldErrors.keys)
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    /**
     * 필수 요청 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        exception: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        
        val fieldErrors = mapOf(
            exception.parameterName to "필수 파라미터가 누락되었습니다"
        )
        
        val validationException = ValidationException(
            message = "필수 요청 파라미터가 누락되었습니다",
            fieldErrors = fieldErrors
        )
        
        val errorResponse = ErrorResponse.from(validationException, request.requestURI)
        
        // 사용자 오류로 INFO 레벨 로깅
        logger.info("Missing required parameter - Path: {}, Parameter: {}", 
            request.requestURI, exception.parameterName)
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    /**
     * HTTP 메시지 읽기 실패 예외 처리
     * JSON 파싱 오류 등
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        
        val invalidRequestException = InvalidRequestException(
            message = "Invalid request format",
            errorCode = "INVALID_REQUEST_FORMAT",
            cause = exception
        )
        
        val errorResponse = ErrorResponse.from(invalidRequestException, request.requestURI)
        
        // 사용자 오류로 INFO 레벨 로깅
        logger.info("Invalid request format - Path: {}, Error: {}", 
            request.requestURI, exception.message)
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    /**
     * 메서드 파라미터 타입 불일치 예외 처리
     * URL 파라미터 타입 변환 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        exception: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        
        val message = "Invalid parameter type: '${exception.value}' for parameter '${exception.name}'"
        val invalidRequestException = InvalidRequestException(
            message = message,
            errorCode = "INVALID_PARAMETER_TYPE"
        )
        
        val errorResponse = ErrorResponse.from(invalidRequestException, request.requestURI)
        
        // 사용자 오류로 INFO 레벨 로깅
        logger.info("Parameter type mismatch - Path: {}, Parameter: {}, Value: {}", 
            request.requestURI, exception.name, exception.value)
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    /**
     * 핸들러를 찾을 수 없는 경우 (404 Not Found)
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(
        exception: NoHandlerFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        
        val notFoundException = ResourceNotFoundException(
            message = "Endpoint not found: ${exception.httpMethod} ${exception.requestURL}",
            errorCode = "ENDPOINT_NOT_FOUND"
        )
        
        val errorResponse = ErrorResponse.from(notFoundException, request.requestURI)
        
        // 사용자 오류로 INFO 레벨 로깅
        logger.info("Endpoint not found - Method: {}, Path: {}", 
            exception.httpMethod, exception.requestURL)
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse)
    }

    /**
     * 예상하지 못한 모든 예외 처리
     * 마지막 방어선 역할
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        
        val errorResponse = ErrorResponse.fromGenericException(
            exception = exception,
            path = request.requestURI
        )
        
        // 시스템 오류로 ERROR 레벨 로깅
        logger.error("Unexpected error occurred - Path: {}, Error: {}", 
            request.requestURI, exception.message, exception)
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }

    /**
     * 예외 로깅 처리
     * 사용자 오류와 시스템 오류를 구분하여 로깅
     */
    private fun logException(
        exception: BaseException,
        request: HttpServletRequest,
        errorResponse: ErrorResponse
    ) {
        val logMessage = "Exception occurred - Path: {}, ErrorCode: {}, Message: {}"
        val logArgs = arrayOf(request.requestURI, exception.errorCode, exception.message)
        
        if (exception.isUserError) {
            // 사용자 오류는 INFO 레벨로 로깅
            logger.info(logMessage, *logArgs)
            
            // 디버그 모드일 때만 스택 트레이스 출력
            if (logger.isDebugEnabled) {
                logger.debug("User error details", exception)
            }
        } else {
            // 시스템 오류는 ERROR 레벨로 로깅 (스택 트레이스 포함)
            logger.error(logMessage, *logArgs, exception)
            
            // 시스템 오류 시 추가 컨텍스트 정보 로깅
            logger.error("Request details - Method: {}, Headers: {}, Parameters: {}", 
                request.method, 
                getRequestHeaders(request),
                request.parameterMap.keys
            )
        }
    }

    /**
     * 요청 헤더 정보 추출 (민감한 정보 제외)
     */
    private fun getRequestHeaders(request: HttpServletRequest): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        val headerNames = request.headerNames
        
        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            // 민감한 헤더 정보는 로깅하지 않음
            if (headerName.lowercase() !in setOf("authorization", "cookie", "x-api-key")) {
                headers[headerName] = request.getHeader(headerName)
            }
        }
        
        return headers
    }
}
