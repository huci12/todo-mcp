package org.library.todo.controller

import org.library.todo.dto.TodoCreateRequest
import org.library.todo.dto.TodoUpdateRequest
import org.library.todo.dto.SessionUser
import org.library.todo.service.TodoService
import org.library.todo.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid

/**
 * Todo 웹 컨트롤러
 * Thymeleaf 템플릿과 연동하여 웹 페이지를 처리하는 컨트롤러
 *
 * @Controller: 뷰를 반환하는 컨트롤러 (Spring MVC)
 * @RestController와 달리 뷰 이름을 반환하여 템플릿 엔진으로 렌더링
 */
@Controller
class TodoWebController(
    /**
     * Todo Service 의존성 주입
     * 생성자 주입 방식 사용
     */
    private val todoService: TodoService,
    private val userService: UserService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(TodoWebController::class.java)
        private const val SESSION_USER_KEY = "user"
    }

    /**
     * Todo 목록 페이지
     *
     * HTTP Method: GET
     * URL: GET /
     *
     * @param session 사용자 세션
     * @param model Spring MVC Model 객체 (뷰에 데이터 전달)
     * @return 뷰 이름 (templates/list.html)
     */
    @GetMapping("/")
    fun listTodos(session: HttpSession, model: Model): String {
        // 로그인 검증
        val sessionUser = getCurrentUser(session) ?: return "redirect:/login"
        
        try {
            // 현재 사용자 정보 조회
            val user = userService.findById(sessionUser.id)
            
            // 해당 사용자의 Todo 목록 조회
            val todos = todoService.getAllTodos(user)

            // 모델에 데이터 추가 (뷰에서 사용할 수 있음)
            model.addAttribute("todos", todos)
            model.addAttribute("title", "Todo 목록")

            logger.info("Todo list page accessed by user: {}, found {} todos", 
                       sessionUser.email, todos.size)

            // templates/list.html 템플릿 반환
            return "list"
        } catch (ex: Exception) {
            logger.error("Error loading todo list for user {}: {}", sessionUser.email, ex.message, ex)
            model.addAttribute("error", "Todo 목록을 불러오는 중 오류가 발생했습니다.")
            model.addAttribute("todos", emptyList<Any>())
            return "list"
        }
    }

    /**
     * Todo 등록 폼 페이지
     *
     * HTTP Method: GET
     * URL: GET /create
     *
     * @param session 사용자 세션
     * @param model Spring MVC Model 객체
     * @return 뷰 이름 (templates/create.html)
     */
    @GetMapping("/create")
    fun createForm(session: HttpSession, model: Model): String {
        // 로그인 검증
        val sessionUser = getCurrentUser(session) ?: return "redirect:/login"
        
        // 빈 TodoCreateRequest 객체를 모델에 추가 (폼 바인딩용)
        model.addAttribute("todoRequest", TodoCreateRequest("", ""))
        model.addAttribute("title", "새 Todo 등록")

        logger.info("Todo create form accessed by user: {}", sessionUser.email)

        // templates/create.html 템플릿 반환
        return "create"
    }

    /**
     * Todo 등록 처리
     *
     * HTTP Method: POST
     * URL: POST /create
     *
     * @param session 사용자 세션
     * @param todoRequest 폼에서 전송된 Todo 생성 요청 데이터 (validation 적용)
     * @param bindingResult validation 결과 객체
     * @param redirectAttributes 리다이렉트 시 전달할 속성
     * @param model Spring MVC Model 객체
     * @return 성공 시 리다이렉트, 실패 시 폼 페이지
     */
    @PostMapping("/create")
    fun createTodo(
        session: HttpSession,
        @Valid @ModelAttribute("todoRequest") todoRequest: TodoCreateRequest,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        // 로그인 검증
        val sessionUser = getCurrentUser(session) ?: return "redirect:/login"
        
        try {
            // Validation 오류가 있는 경우
            if (bindingResult.hasErrors()) {
                logger.warn("Validation errors in todo creation by user {}: {}", 
                           sessionUser.email, bindingResult.allErrors)
                model.addAttribute("title", "새 Todo 등록")
                return "create" // 폼 페이지로 다시 이동 (에러 메시지와 함께)
            }

            // 현재 사용자 정보 조회
            val user = userService.findById(sessionUser.id)

            // 입력값 정규화 후 Todo 생성
            val normalizedRequest = todoRequest.normalize()
            val createdTodo = todoService.createTodo(user, normalizedRequest)

            logger.info(
                "Todo created successfully: id={}, title='{}' by user: {}",
                createdTodo.id, createdTodo.title, sessionUser.email
            )

            // 성공 메시지 추가 (리다이렉트 후에도 유지됨)
            redirectAttributes.addFlashAttribute(
                "message",
                "새로운 Todo '${createdTodo.title}'가 성공적으로 등록되었습니다."
            )

            // 목록 페이지로 리다이렉트 (PRG 패턴)
            return "redirect:/"

        } catch (ex: Exception) {
            logger.error("Error creating todo by user {}: {}", sessionUser.email, ex.message, ex)
            model.addAttribute("error", "Todo 등록 중 오류가 발생했습니다: ${ex.message}")
            model.addAttribute("title", "새 Todo 등록")
            return "create"
        }
    }

    /**
     * Todo 완료 상태 토글
     * 체크박스 클릭 시 호출되어 isDone 상태를 변경
     * 
     * HTTP Method: POST
     * URL: POST /toggle/{id}
     * 
     * @param session 사용자 세션
     * @param id 상태를 변경할 Todo ID
     * @param redirectAttributes 리다이렉트 시 전달할 속성
     * @return 목록 페이지로 리다이렉트
     */
    @PostMapping("/toggle/{id}")
    fun toggleTodo(
        session: HttpSession,
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        // 로그인 검증
        val sessionUser = getCurrentUser(session) ?: return "redirect:/login"
        
        try {
            // 현재 사용자 정보 조회
            val user = userService.findById(sessionUser.id)
            
            // 현재 Todo 조회 (권한 체크 포함)
            val currentTodo = todoService.getTodoById(user, id)

            // 완료 상태 반전
            val updateRequest = TodoUpdateRequest(isDone = !currentTodo.isDone)
            val updatedTodo = todoService.updateTodo(user, id, updateRequest)

            logger.info("Todo status toggled: id={}, isDone={} by user: {}", 
                       id, updatedTodo.isDone, sessionUser.email)

            // 성공 메시지 추가
            val statusMessage = if (updatedTodo.isDone) "완료" else "미완료"
            redirectAttributes.addFlashAttribute(
                "message",
                "'${updatedTodo.title}'가 ${statusMessage}로 변경되었습니다."
            )

        } catch (ex: Exception) {
            logger.error("Error toggling todo status: id={}, user={}, error={}", 
                        id, sessionUser.email, ex.message, ex)
            redirectAttributes.addFlashAttribute(
                "error",
                "Todo 상태 변경 중 오류가 발생했습니다: ${ex.message}"
            )
        }

        // 목록 페이지로 리다이렉트
        return "redirect:/"
    }

    /**
     * Todo 삭제
     * 
     * HTTP Method: POST
     * URL: POST /delete/{id}
     * 
     * @param session 사용자 세션
     * @param id 삭제할 Todo ID
     * @param redirectAttributes 리다이렉트 시 전달할 속성
     * @return 목록 페이지로 리다이렉트
     */
    @PostMapping("/delete/{id}")
    fun deleteTodo(
        session: HttpSession,
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        // 로그인 검증
        val sessionUser = getCurrentUser(session) ?: return "redirect:/login"
        
        try {
            // 현재 사용자 정보 조회
            val user = userService.findById(sessionUser.id)
            
            // 삭제 전 제목 조회 (메시지용, 권한 체크 포함)
            val todoToDelete = todoService.getTodoById(user, id)
            val todoTitle = todoToDelete.title

            // Todo 삭제
            todoService.deleteTodo(user, id)

            logger.info("Todo deleted successfully: id={}, title='{}' by user: {}", 
                       id, todoTitle, sessionUser.email)

            // 성공 메시지 추가
            redirectAttributes.addFlashAttribute(
                "message",
                "'${todoTitle}'가 성공적으로 삭제되었습니다."
            )

        } catch (ex: Exception) {
            logger.error("Error deleting todo: id={}, user={}, error={}", 
                        id, sessionUser.email, ex.message, ex)
            redirectAttributes.addFlashAttribute(
                "error",
                "Todo 삭제 중 오류가 발생했습니다: ${ex.message}"
            )
        }

        // 목록 페이지로 리다이렉트
        return "redirect:/"
    }
    
    /**
     * 현재 로그인한 사용자 정보 조회 (유틸리티 메서드)
     */
    private fun getCurrentUser(session: HttpSession): SessionUser? {
        return session.getAttribute(SESSION_USER_KEY) as? SessionUser
    }
}
