package org.library.todo.controller

import org.library.todo.dto.TodoCreateRequest
import org.library.todo.dto.TodoResponse
import org.library.todo.dto.TodoUpdateRequest
import org.library.todo.dto.TodoSearchRequest
import org.library.todo.service.TodoService
import org.library.todo.exception.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated

/**
 * Todo REST API 컨트롤러
 * Todo 관련 HTTP 요청을 처리하는 컨트롤러 계층
 * 
 * @RestController: @Controller + @ResponseBody
 * JSON 형태의 응답을 자동으로 직렬화
 * 
 * @RequestMapping: 기본 URL 경로 설정
 * 모든 엔드포인트는 "/api/todos"로 시작
 */
@RestController
@RequestMapping("/api/todos")
@Validated
class TodoController(
    /**
     * Todo Service 의존성 주입
     * 생성자 주입 방식 사용
     */
    private val todoService: TodoService
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(TodoController::class.java)
    }

/**
 * 루트 경로용 Todo 목록 컨트롤러
 * /list 엔드포인트 전용 처리
 */
@RestController
class TodoListController(
    private val todoService: TodoService
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(TodoListController::class.java)
    }
    
    /**
     * 루트 경로에서 Todo 목록 조회
     * 
     * HTTP Method: GET
     * URL: GET /list
     * 
     * @return Todo 목록과 적절한 HTTP 상태 코드
     * 
     * 500 에러 대신 적절한 상태 코드와 빈 배열 반환
     */
    @GetMapping("/list")
    fun getTodoList(): ResponseEntity<List<TodoResponse>> {
        return try {
            logger.info("Received request for /list endpoint")
            val todos = todoService.getAllTodos()
            
            logger.info("Successfully retrieved {} todos from /list endpoint", todos.size)
            ResponseEntity.ok(todos)
            
        } catch (ex: ResourceNotFoundException) {
            logger.info("No todos found at /list endpoint, returning empty list")
            ResponseEntity.ok(emptyList())
            
        } catch (ex: InvalidRequestException) {
            logger.warn("Invalid request at /list endpoint: {}", ex.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyList())
            
        } catch (ex: ValidationException) {
            logger.warn("Validation error at /list endpoint: {}", ex.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyList())
            
        } catch (ex: SystemErrorException) {
            logger.error("System error at /list endpoint: {}", ex.message, ex)
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(emptyList())
            
        } catch (ex: Exception) {
            logger.error("Unexpected error at /list endpoint: {}", ex.message, ex)
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(emptyList())
        }
    }
}
    
    /**
     * 새로운 Todo를 생성하는 엔드포인트
     * 
     * HTTP Method: POST
     * URL: POST /api/todos
     * 
     * @param request 클라이언트에서 전송한 Todo 생성 요청 데이터 (validation 적용)
     * @return 생성된 Todo 정보와 201 Created 상태 코드
     * 
     * @PostMapping: HTTP POST 요청을 처리
     * @RequestBody: HTTP 요청 본문의 JSON을 객체로 변환
     * @Valid: 요청 본문 validation 적용
     */
    @PostMapping
    fun createTodo(@Valid @RequestBody request: TodoCreateRequest): ResponseEntity<TodoResponse> {
        // 입력값 정규화 (공백 제거 등)
        val normalizedRequest = request.normalize()
        val response = todoService.createTodo(normalizedRequest)
        // 201 Created 상태 코드와 함께 생성된 리소스 정보 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    /**
     * 모든 Todo를 조회하는 엔드포인트
     * 
     * HTTP Method: GET
     * URL: GET /api/todos
     * 
     * @return 모든 Todo 목록과 200 OK 상태 코드
     * 
     * @GetMapping: HTTP GET 요청을 처리
     */
    @GetMapping
    fun getAllTodos(): ResponseEntity<List<TodoResponse>> {
        return try {
            val response = todoService.getAllTodos()
            // 200 OK 상태 코드와 함께 Todo 목록 반환
            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            // 예외 발생 시 빈 배열과 200 OK 반환 (서비스 가용성 유지)
            ResponseEntity.ok(emptyList())
        }
    }
    
    /**
     * Todo 목록을 조회하는 별칭 엔드포인트
     * 
     * HTTP Method: GET
     * URL: GET /api/todos/list
     * 
     * @return 모든 Todo 목록과 적절한 HTTP 상태 코드
     * 
     * 에러 발생 시 500 에러가 아닌 적절한 상태 코드와 빈 배열 반환
     */
    @GetMapping("/list")
    fun listTodos(): ResponseEntity<List<TodoResponse>> {
        return try {
            logger.info("Received request for todo list")
            val response = todoService.getAllTodos()
            
            logger.info("Successfully retrieved {} todos", response.size)
            // 데이터가 있으면 200 OK, 없으면 빈 배열과 200 OK
            ResponseEntity.ok(response)
            
        } catch (ex: ResourceNotFoundException) {
            // 리소스를 찾을 수 없는 경우 - 빈 배열과 200 OK
            logger.info("No todos found, returning empty list")
            ResponseEntity.ok(emptyList())
            
        } catch (ex: InvalidRequestException) {
            // 잘못된 요청인 경우 - 빈 배열과 400 Bad Request
            logger.warn("Invalid request for todo list: {}", ex.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyList())
            
        } catch (ex: ValidationException) {
            // 검증 실패인 경우 - 빈 배열과 400 Bad Request  
            logger.warn("Validation error in todo list request: {}", ex.message)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyList())
            
        } catch (ex: SystemErrorException) {
            // 시스템 에러인 경우 - 빈 배열과 503 Service Unavailable
            logger.error("System error occurred while fetching todo list: {}", ex.message, ex)
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(emptyList())
            
        } catch (ex: Exception) {
            // 기타 예외인 경우 - 빈 배열과 503 Service Unavailable
            logger.error("Unexpected error occurred while fetching todo list: {}", ex.message, ex)
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(emptyList())
        }
    }
    
    /**
     * Todo를 검색/필터링하여 조회하는 엔드포인트
     * 
     * HTTP Method: GET
     * URL: GET /api/todos/search
     * 
     * @param searchRequest 검색 조건 (페이징, 필터링 등)
     * @return 검색된 Todo 목록과 200 OK 상태 코드
     * 
     * @Valid: 요청 파라미터 validation 적용
     */
    @GetMapping("/search")
    fun searchTodos(@Valid searchRequest: TodoSearchRequest): ResponseEntity<List<TodoResponse>> {
        val response = todoService.searchTodos(searchRequest)
        return ResponseEntity.ok(response)
    }
    
    /**
     * 특정 ID의 Todo를 조회하는 엔드포인트
     * 
     * HTTP Method: GET
     * URL: GET /api/todos/{id}
     * 
     * @param id URL 경로에서 추출한 Todo ID (양수만 허용)
     * @return 조회된 Todo 정보와 200 OK 상태 코드
     * 
     * @GetMapping("/{id}"): 경로 변수를 포함한 GET 요청 처리
     * @PathVariable: URL 경로의 변수를 메서드 파라미터로 바인딩
     * @Positive: ID가 양수인지 검증
     * 
     * 예외는 GlobalExceptionHandler에서 처리됨
     */
    @GetMapping("/{id}")
    fun getTodoById(
        @PathVariable 
        @Positive(message = "Todo ID는 1 이상의 양수여야 합니다") 
        id: Long
    ): ResponseEntity<TodoResponse> {
        val response = todoService.getTodoById(id)
        return ResponseEntity.ok(response)
    }
    
    /**
     * 기존 Todo를 수정하는 엔드포인트
     * 
     * HTTP Method: PUT
     * URL: PUT /api/todos/{id}
     * 
     * @param id URL 경로에서 추출한 수정할 Todo ID (양수만 허용)
     * @param request 클라이언트에서 전송한 Todo 수정 요청 데이터 (validation 적용)
     * @return 수정된 Todo 정보와 200 OK 상태 코드
     * 
     * @PutMapping("/{id}"): 경로 변수를 포함한 PUT 요청 처리
     * @PathVariable: URL 경로의 변수를 메서드 파라미터로 바인딩
     * @RequestBody: HTTP 요청 본문의 JSON을 객체로 변환
     * @Valid: 요청 본문 validation 적용
     * @Positive: ID가 양수인지 검증
     * 
     * 예외는 GlobalExceptionHandler에서 처리됨
     */
    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable 
        @Positive(message = "Todo ID는 1 이상의 양수여야 합니다") 
        id: Long,
        @Valid @RequestBody request: TodoUpdateRequest
    ): ResponseEntity<TodoResponse> {
        // 입력값 정규화 (공백 제거 등)
        val normalizedRequest = request.normalize()
        val response = todoService.updateTodo(id, normalizedRequest)
        return ResponseEntity.ok(response)
    }
    
    /**
     * Todo를 삭제하는 엔드포인트
     * 
     * HTTP Method: DELETE
     * URL: DELETE /api/todos/{id}
     * 
     * @param id URL 경로에서 추출한 삭제할 Todo ID (양수만 허용)
     * @return 204 No Content 상태 코드
     * 
     * @DeleteMapping("/{id}"): 경로 변수를 포함한 DELETE 요청 처리
     * @PathVariable: URL 경로의 변수를 메서드 파라미터로 바인딩
     * @Positive: ID가 양수인지 검증
     * ResponseEntity<Void>: 응답 본문이 없음을 명시
     * 
     * 예외는 GlobalExceptionHandler에서 처리됨
     */
    @DeleteMapping("/{id}")
    fun deleteTodo(
        @PathVariable 
        @Positive(message = "Todo ID는 1 이상의 양수여야 합니다") 
        id: Long
    ): ResponseEntity<Void> {
        todoService.deleteTodo(id)
        // 삭제 성공 시 204 No Content 반환 (응답 본문 없음)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 특정 완료 상태의 Todo들을 일괄 삭제하는 엔드포인트
     * 
     * HTTP Method: DELETE
     * URL: DELETE /api/todos/bulk?isDone={true|false}
     * 
     * @param isDone 삭제할 Todo들의 완료 상태 (필수)
     * @return 삭제된 Todo 개수와 200 OK 상태 코드
     * 
     * @RequestParam: 쿼리 파라미터 바인딩
     * @NotNull: null 값 검증
     */
    @DeleteMapping("/bulk")
    fun deleteTodosByStatus(
        @RequestParam("isDone") 
        @jakarta.validation.constraints.NotNull(message = "완료 상태는 필수 입력 항목입니다") 
        isDone: Boolean
    ): ResponseEntity<Map<String, Any>> {
        val deletedCount = todoService.deleteTodosByStatus(isDone)
        val response = mapOf(
            "deletedCount" to deletedCount,
            "message" to "${if (isDone) "완료된" else "미완료"} Todo ${deletedCount}개가 삭제되었습니다"
        )
        return ResponseEntity.ok(response)
    }
}
