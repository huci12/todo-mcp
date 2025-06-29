package org.library.todo.exception

/**
 * Todo 도메인 전용 예외 클래스들
 */

/**
 * Todo를 찾을 수 없는 경우
 */
class TodoNotFoundException(
    todoId: Long
) : ResourceNotFoundException(
    message = "Todo not found with id: $todoId",
    errorCode = "TODO_NOT_FOUND"
)

/**
 * 사용자를 찾을 수 없는 경우
 */
class UserNotFoundException(
    email: String
) : ResourceNotFoundException(
    message = "User not found with email: $email",
    errorCode = "USER_NOT_FOUND"
)

/**
 * 이메일 중복 예외
 */
class DuplicateEmailException(
    email: String
) : InvalidRequestException(
    message = "Email already exists: $email",
    errorCode = "DUPLICATE_EMAIL"
)

/**
 * 비밀번호 불일치 예외
 */
class PasswordMismatchException : ValidationException(
    message = "Password and password confirmation do not match",
    fieldErrors = mapOf("passwordConfirm" to "비밀번호와 비밀번호 확인이 일치하지 않습니다"),
    errorCode = "PASSWORD_MISMATCH"
)

/**
 * 잘못된 로그인 정보 예외
 */
class InvalidCredentialsException : InvalidRequestException(
    message = "Invalid email or password",
    errorCode = "INVALID_CREDENTIALS"
)

/**
 * 권한 없음 예외 (다른 사용자의 Todo 접근 시)
 */
class UnauthorizedAccessException(
    todoId: Long
) : InvalidRequestException(
    message = "Unauthorized access to todo with id: $todoId",
    errorCode = "UNAUTHORIZED_ACCESS"
)

/**
 * Todo 제목이 유효하지 않은 경우
 */
class InvalidTodoTitleException(
    title: String?
) : ValidationException(
    message = "Invalid todo title: '$title'",
    fieldErrors = mapOf("title" to "Title must not be blank and must be between 1 and 100 characters"),
    errorCode = "INVALID_TODO_TITLE"
)

/**
 * Todo 설명이 유효하지 않은 경우
 */
class InvalidTodoDescriptionException(
    description: String?
) : ValidationException(
    message = "Invalid todo description: '$description'",
    fieldErrors = mapOf("description" to "Description must not exceed 500 characters"),
    errorCode = "INVALID_TODO_DESCRIPTION"
)

/**
 * Todo 상태 변경이 유효하지 않은 경우
 */
class InvalidTodoStatusException(
    currentStatus: Boolean,
    requestedStatus: Boolean
) : InvalidRequestException(
    message = "Cannot change todo status from $currentStatus to $requestedStatus",
    errorCode = "INVALID_TODO_STATUS_CHANGE"
)

/**
 * Todo 생성/수정 시 필수 필드가 누락된 경우
 */
class TodoRequiredFieldException(
    missingFields: List<String>
) : ValidationException(
    message = "Required fields are missing: ${missingFields.joinToString(", ")}",
    fieldErrors = missingFields.associateWith { "This field is required" },
    errorCode = "TODO_REQUIRED_FIELD_MISSING"
)
