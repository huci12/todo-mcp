package org.library.todo.service

import org.library.todo.dto.TodoCreateRequest
import org.library.todo.dto.TodoResponse
import org.library.todo.dto.TodoUpdateRequest
import org.library.todo.dto.TodoSearchRequest
import org.library.todo.entity.Todo
import org.library.todo.entity.User
import org.library.todo.exception.*
import org.library.todo.repository.TodoRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Todo 서비스 클래스
 * Todo 관련 비즈니스 로직을 처리하는 서비스 계층
 *
 * @Transactional(readOnly = true): 기본적으로 읽기 전용 트랜잭션으로 설정
 * 성능 최적화 및 데이터 일관성 보장
 */
@Service
@Transactional(readOnly = true)
class TodoService(
    /**
     * Todo Repository 의존성 주입
     * 생성자 주입 방식 사용
     */
    private val todoRepository: TodoRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(TodoService::class.java)
    }

    /**
     * 새로운 Todo를 생성하는 메서드 (사용자별)
     *
     * @param user 로그인한 사용자
     * @param request Todo 생성 요청 DTO
     * @return 생성된 Todo 정보를 담은 응답 DTO
     * @throws InvalidTodoTitleException 제목이 유효하지 않은 경우
     * @throws InvalidTodoDescriptionException 설명이 유효하지 않은 경우
     * @Transactional: 쓰기 작업이므로 읽기 전용 트랜잭션 설정을 오버라이드
     */
    @Transactional
    fun createTodo(user: User, request: TodoCreateRequest): TodoResponse {
        // 입력 검증
        validateTodoInput(request.title, request.description)

        try {
            // 요청 DTO를 기반으로 새로운 Todo 엔티티 생성
            val todo = Todo(
                title = request.title,
                description = request.description,
                isDone = false, // 기본값 false
                user = user // 로그인한 사용자 설정
            )

            // 데이터베이스에 저장
            val savedTodo = todoRepository.save(todo)

            logger.info("Todo created successfully with id: {} for user: {}", savedTodo.id, user.email)

            // 저장된 엔티티를 응답 DTO로 변환하여 반환
            return TodoResponse.from(savedTodo)
        } catch (ex: Exception) {
            logger.error("Failed to create todo for user {}: {}", user.email, ex.message, ex)
            throw SystemErrorException(
                message = "Failed to create todo",
                errorCode = "TODO_CREATE_FAILED",
                cause = ex
            )
        }
    }

    /**
     * 특정 사용자의 모든 Todo를 조회하는 메서드
     *
     * @param user 조회할 사용자
     * @return Todo 목록을 담은 응답 DTO 리스트
     */
    fun getAllTodos(user: User): List<TodoResponse> {
        return todoRepository.findByUser(user)
            .map { TodoResponse.from(it) } // 각 엔티티를 DTO로 변환
    }

    /**
     * ID로 특정 Todo를 조회하는 메서드 (권한 체크 포함)
     *
     * @param user 로그인한 사용자
     * @param id 조회할 Todo의 ID
     * @return Todo 정보를 담은 응답 DTO
     * @throws TodoNotFoundException 해당 ID의 Todo가 존재하지 않는 경우
     * @throws UnauthorizedAccessException 다른 사용자의 Todo에 접근하는 경우
     */
    fun getTodoById(user: User, id: Long): TodoResponse {
        try {
            // 해당 사용자의 Todo만 조회
            val todo = todoRepository.findByIdAndUser(id, user)
                ?: throw TodoNotFoundException(id)

            return TodoResponse.from(todo)
        } catch (ex: TodoNotFoundException) {
            throw ex // 이미 올바른 예외이므로 다시 throw
        } catch (ex: Exception) {
            logger.error("Failed to get todo by id {} for user {}: {}", id, user.email, ex.message, ex)
            throw SystemErrorException(
                message = "Failed to retrieve todo",
                errorCode = "TODO_RETRIEVE_FAILED",
                cause = ex
            )
        }
    }

    /**
     * 기존 Todo를 수정하는 메서드 (권한 체크 포함)
     * 부분 업데이트 지원 (null이 아닌 필드만 수정)
     *
     * @param user 로그인한 사용자
     * @param id 수정할 Todo의 ID
     * @param request Todo 수정 요청 DTO
     * @return 수정된 Todo 정보를 담은 응답 DTO
     * @throws TodoNotFoundException 해당 ID의 Todo가 존재하지 않는 경우
     * @throws UnauthorizedAccessException 다른 사용자의 Todo에 접근하는 경우
     * @throws InvalidTodoTitleException 제목이 유효하지 않은 경우
     * @throws InvalidTodoDescriptionException 설명이 유효하지 않은 경우
     * @Transactional: 쓰기 작업이므로 읽기 전용 트랜잭션 설정을 오버라이드
     */
    @Transactional
    fun updateTodo(user: User, id: Long, request: TodoUpdateRequest): TodoResponse {
        try {
            // 해당 사용자의 Todo만 조회
            val existingTodo = todoRepository.findByIdAndUser(id, user)
                ?: throw TodoNotFoundException(id)

            // 수정할 값들에 대한 검증
            val newTitle = request.title ?: existingTodo.title
            val newDescription = request.description ?: existingTodo.description

            // 새로운 값들이 유효한지 검증
            validateTodoInput(newTitle, newDescription)

            // 새로운 Todo 엔티티 생성 (부분 업데이트)
            // 요청에서 null이 아닌 값만 업데이트, null이면 기존 값 유지
            val updatedTodo = Todo(
                id = existingTodo.id, // ID는 유지
                title = newTitle,
                description = newDescription,
                isDone = request.isDone ?: existingTodo.isDone,
                user = existingTodo.user // 사용자는 유지
            )

            // 수정된 엔티티 저장
            val savedTodo = todoRepository.save(updatedTodo)

            logger.info("Todo updated successfully with id: {} for user: {}", savedTodo.id, user.email)

            return TodoResponse.from(savedTodo)
        } catch (ex: BaseException) {
            throw ex // 이미 올바른 예외이므로 다시 throw
        } catch (ex: Exception) {
            logger.error("Failed to update todo with id {} for user {}: {}", id, user.email, ex.message, ex)
            throw SystemErrorException(
                message = "Failed to update todo",
                errorCode = "TODO_UPDATE_FAILED",
                cause = ex
            )
        }
    }

    /**
     * Todo를 삭제하는 메서드 (권한 체크 포함)
     *
     * @param user 로그인한 사용자
     * @param id 삭제할 Todo의 ID
     * @throws TodoNotFoundException 해당 ID의 Todo가 존재하지 않는 경우
     * @throws UnauthorizedAccessException 다른 사용자의 Todo에 접근하는 경우
     * @Transactional: 쓰기 작업이므로 읽기 전용 트랜잭션 설정을 오버라이드
     */
    @Transactional
    fun deleteTodo(user: User, id: Long) {
        try {
            // 해당 사용자의 Todo만 조회
            val todo = todoRepository.findByIdAndUser(id, user)
                ?: throw TodoNotFoundException(id)

            // ID로 Todo 삭제
            todoRepository.deleteById(todo.id)

            logger.info("Todo deleted successfully with id: {} for user: {}", id, user.email)
        } catch (ex: TodoNotFoundException) {
            throw ex // 이미 올바른 예외이므로 다시 throw
        } catch (ex: Exception) {
            logger.error("Failed to delete todo with id {} for user {}: {}", id, user.email, ex.message, ex)
            throw SystemErrorException(
                message = "Failed to delete todo",
                errorCode = "TODO_DELETE_FAILED",
                cause = ex
            )
        }
    }

    /**
     * 특정 사용자의 Todo를 검색/필터링하여 조회하는 메서드
     *
     * @param user 조회할 사용자
     * @param searchRequest 검색 조건 (페이징, 필터링 등)
     * @return 검색된 Todo 목록을 담은 응답 DTO 리스트
     */
    fun searchTodos(user: User, searchRequest: TodoSearchRequest): List<TodoResponse> {
        try {
            // 해당 사용자의 Todo만 조회
            var todos = todoRepository.findByUser(user)

            // 완료 상태로 필터링
            searchRequest.isDone?.let { isDone ->
                todos = todos.filter { it.isDone == isDone }
            }

            // 제목 키워드로 검색
            searchRequest.titleKeyword?.let { keyword ->
                todos = todos.filter { it.title.contains(keyword, ignoreCase = true) }
            }

            // 페이징 처리
            val startIndex = searchRequest.page * searchRequest.size
            val endIndex = minOf(startIndex + searchRequest.size, todos.size)

            val pagedTodos = if (startIndex < todos.size) {
                todos.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

            logger.info("Found {} todos with search criteria for user: {}", pagedTodos.size, user.email)

            return pagedTodos.map { TodoResponse.from(it) }
        } catch (ex: Exception) {
            logger.error("Failed to search todos for user {}: {}", user.email, ex.message, ex)
            throw SystemErrorException(
                message = "Failed to search todos",
                errorCode = "TODO_SEARCH_FAILED",
                cause = ex
            )
        }
    }

    /**
     * 특정 사용자의 완료 상태별 Todo들을 일괄 삭제하는 메서드
     *
     * @param user 로그인한 사용자
     * @param isDone 삭제할 Todo들의 완료 상태
     * @return 삭제된 Todo 개수
     * @Transactional: 쓰기 작업이므로 읽기 전용 트랜잭션 설정을 오버라이드
     */
    @Transactional
    fun deleteTodosByStatus(user: User, isDone: Boolean): Int {
        try {
            // 해당 사용자의 특정 상태 Todo들 조회
            val todosToDelete = todoRepository.findByUserAndIsDone(user, isDone)
            val deleteCount = todosToDelete.size

            // 일괄 삭제
            todosToDelete.forEach { todo ->
                todoRepository.deleteById(todo.id)
            }

            logger.info("Deleted {} todos with status: {} for user: {}", deleteCount, isDone, user.email)

            return deleteCount
        } catch (ex: Exception) {
            logger.error("Failed to delete todos by status {} for user {}: {}", isDone, user.email, ex.message, ex)
            throw SystemErrorException(
                message = "Failed to delete todos by status",
                errorCode = "TODO_BULK_DELETE_FAILED",
                cause = ex
            )
        }
    }

    /**
     * 관리자용 전체 Todo 조회 (기존 메서드 호환성 유지)
     */
    fun getAllTodos(): List<TodoResponse> {
        return todoRepository.findAll()
            .map { TodoResponse.from(it) }
    }

    /**
     * 관리자용 ID 조회 (기존 메서드 호환성 유지)
     */
    fun getTodoById(id: Long): TodoResponse {
        val todo = todoRepository.findByIdOrNull(id)
            ?: throw TodoNotFoundException(id)
        return TodoResponse.from(todo)
    }

    /**
     * 기존 메서드들 (사용자 없이) - 호환성 유지
     */
    @Transactional
    fun createTodo(request: TodoCreateRequest): TodoResponse {
        throw InvalidRequestException(
            message = "User context required for creating todo",
            errorCode = "USER_REQUIRED"
        )
    }

    @Transactional
    fun updateTodo(id: Long, request: TodoUpdateRequest): TodoResponse {
        throw InvalidRequestException(
            message = "User context required for updating todo",
            errorCode = "USER_REQUIRED"
        )
    }

    @Transactional
    fun deleteTodo(id: Long) {
        throw InvalidRequestException(
            message = "User context required for deleting todo",
            errorCode = "USER_REQUIRED"
        )
    }

    fun searchTodos(searchRequest: TodoSearchRequest): List<TodoResponse> {
        throw InvalidRequestException(
            message = "User context required for searching todos",
            errorCode = "USER_REQUIRED"
        )
    }

    @Transactional
    fun deleteTodosByStatus(isDone: Boolean): Int {
        throw InvalidRequestException(
            message = "User context required for bulk deleting todos",
            errorCode = "USER_REQUIRED"
        )
    }

    /**
     * Todo 입력값 검증 메서드
     *
     * @param title Todo 제목
     * @param description Todo 설명
     * @throws InvalidTodoTitleException 제목이 유효하지 않은 경우
     * @throws InvalidTodoDescriptionException 설명이 유효하지 않은 경우
     */
    private fun validateTodoInput(title: String, description: String?) {
        // 제목 검증
        if (title.isBlank()) {
            throw InvalidTodoTitleException(title)
        }
        if (title.length > 200) {
            throw InvalidTodoTitleException(title)
        }

        // 설명 검증 (null 허용)
        if (description != null && description.length > 1000) {
            throw InvalidTodoDescriptionException(description)
        }
    }
}
