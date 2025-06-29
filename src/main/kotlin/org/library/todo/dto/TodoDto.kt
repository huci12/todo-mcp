package org.library.todo.dto

import org.library.todo.entity.Todo
import jakarta.validation.constraints.*
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * Todo 생성 요청 DTO
 * 클라이언트에서 새로운 Todo를 생성할 때 사용하는 데이터 전송 객체
 */
data class TodoCreateRequest(
    /**
     * 할 일 제목 (필수)
     * 빈 문자열이나 null이 될 수 없음
     * 길이 제한: 1~200자
     * 앞뒤 공백은 자동으로 제거됨
     */
    @field:NotBlank(message = "제목은 필수 입력 항목입니다")
    @field:Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하로 입력해주세요")
    @field:Pattern(
        regexp = "^(?!\\s*$).+",
        message = "제목은 공백만으로 구성될 수 없습니다"
    )
    val title: String,

    /**
     * 할 일 상세 설명 (선택)
     * null 허용, 기본값은 null
     * 길이 제한: 최대 1000자
     * 빈 문자열인 경우 null로 처리
     */
    @field:Size(max = 1000, message = "설명은 1000자 이하로 입력해주세요")
    val description: String? = null
) {
    /**
     * 입력값 정규화 (생성자 후 처리)
     * - 제목의 앞뒤 공백 제거
     * - 설명이 빈 문자열인 경우 null로 변환
     */
    fun normalize(): TodoCreateRequest {
        return this.copy(
            title = this.title.trim(),
            description = this.description?.takeIf { it.isNotBlank() }?.trim()
        )
    }
}

/**
 * Todo 수정 요청 DTO
 * 기존 Todo의 일부 필드만 수정할 때 사용하는 데이터 전송 객체
 * 모든 필드가 선택사항이므로 부분 업데이트 가능
 */
@ValidUpdateRequest
data class TodoUpdateRequest(
    /**
     * 수정할 제목 (선택)
     * null이면 기존 제목 유지
     * 값이 있는 경우 길이 제한: 1~200자
     * 공백만으로 구성될 수 없음
     */
    @field:Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하로 입력해주세요")
    @field:Pattern(
        regexp = "^(?!\\s*$).+|^$",
        message = "제목은 공백만으로 구성될 수 없습니다"
    )
    val title: String? = null,

    /**
     * 수정할 설명 (선택)
     * null이면 기존 설명 유지
     * 값이 있는 경우 길이 제한: 최대 1000자
     */
    @field:Size(max = 1000, message = "설명은 1000자 이하로 입력해주세요")
    val description: String? = null,

    /**
     * 수정할 완료 상태 (선택)
     * null이면 기존 완료 상태 유지
     */
    val isDone: Boolean? = null
) {
    /**
     * 입력값 정규화 (생성자 후 처리)
     * - 제목과 설명의 앞뒤 공백 제거
     * - 빈 문자열인 경우 null로 변환
     */
    fun normalize(): TodoUpdateRequest {
        return this.copy(
            title = this.title?.takeIf { it.isNotBlank() }?.trim(),
            description = this.description?.takeIf { it.isNotBlank() }?.trim()
        )
    }
}

/**
 * TodoUpdateRequest를 위한 커스텀 Validation 어노테이션
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidUpdateRequestValidator::class])
annotation class ValidUpdateRequest(
    val message: String = "최소 하나 이상의 필드는 수정 값을 포함해야 합니다",
    val groups: Array<kotlin.reflect.KClass<*>> = [],
    val payload: Array<kotlin.reflect.KClass<out Any>> = []
)

/**
 * ValidUpdateRequest 어노테이션을 위한 Validator 클래스
 */
class ValidUpdateRequestValidator : ConstraintValidator<ValidUpdateRequest, TodoUpdateRequest> {
    override fun isValid(value: TodoUpdateRequest?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return value.title != null || value.description != null || value.isDone != null
    }
}

/**
 * Todo 응답 DTO
 * 클라이언트에게 Todo 정보를 반환할 때 사용하는 데이터 전송 객체
 * Entity의 모든 정보를 포함
 */
data class TodoResponse(
    /**
     * Todo의 고유 식별자
     */
    val id: Long,

    /**
     * 할 일 제목
     */
    val title: String,

    /**
     * 할 일 상세 설명
     * null 가능
     */
    val description: String?,

    /**
     * 완료 여부
     * true: 완료, false: 미완료
     */
    val isDone: Boolean
) {
    companion object {
        /**
         * Todo Entity를 TodoResponse DTO로 변환하는 팩토리 메서드
         * @param todo 변환할 Todo 엔티티
         * @return TodoResponse DTO 객체
         */
        fun from(todo: Todo): TodoResponse {
            return TodoResponse(
                id = todo.id,
                title = todo.title,
                description = todo.description,
                isDone = todo.isDone
            )
        }
    }
}

/**
 * Todo 목록 조회 요청 DTO (페이징, 필터링용)
 */
data class TodoSearchRequest(
    /**
     * 페이지 번호 (0부터 시작)
     */
    @field:Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    val page: Int = 0,

    /**
     * 페이지 크기
     */
    @field:Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    val size: Int = 10,

    /**
     * 완료 상태 필터 (null이면 전체 조회)
     */
    val isDone: Boolean? = null,

    /**
     * 제목 검색 키워드 (null이면 검색하지 않음)
     */
    @field:Size(max = 100, message = "검색 키워드는 100자 이하로 입력해주세요")
    val titleKeyword: String? = null
)
