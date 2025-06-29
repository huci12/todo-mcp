package org.library.todo.repository

import org.library.todo.entity.Todo
import org.library.todo.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Todo Repository 인터페이스
 * Todo 엔티티에 대한 데이터베이스 CRUD 작업을 담당
 * 
 * JpaRepository를 상속받아 기본적인 CRUD 메서드들을 자동으로 제공받음:
 * - save(): 엔티티 저장/수정
 * - findById(): ID로 엔티티 조회
 * - findAll(): 모든 엔티티 조회
 * - deleteById(): ID로 엔티티 삭제
 * - existsById(): ID 존재 여부 확인
 * 
 * @param Todo 관리할 엔티티 타입
 * @param Long 엔티티의 기본키 타입
 */
@Repository
interface TodoRepository : JpaRepository<Todo, Long> {
    
    /**
     * 특정 사용자의 모든 Todo 조회
     * 
     * @param user 조회할 사용자
     * @return 해당 사용자의 Todo 목록
     */
    fun findByUser(user: User): List<Todo>
    
    /**
     * 특정 사용자의 Todo를 완료 상태별로 조회
     * 
     * @param user 조회할 사용자
     * @param isDone 완료 상태 (true: 완료, false: 미완료)
     * @return 해당 사용자의 특정 완료 상태 Todo 목록
     */
    fun findByUserAndIsDone(user: User, isDone: Boolean): List<Todo>
    
    /**
     * 특정 사용자의 Todo 중 ID로 조회
     * 권한 체크를 위해 사용
     * 
     * @param id Todo ID
     * @param user 사용자
     * @return 해당 사용자의 Todo가 있으면 Todo 객체, 없으면 null
     */
    fun findByIdAndUser(id: Long, user: User): Todo?
    
    /**
     * 특정 사용자의 완료 상태별 Todo 개수 조회
     * 
     * @param user 조회할 사용자
     * @param isDone 완료 상태
     * @return 해당 조건의 Todo 개수
     */
    fun countByUserAndIsDone(user: User, isDone: Boolean): Long
}
