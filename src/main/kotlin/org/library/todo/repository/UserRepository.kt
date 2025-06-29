package org.library.todo.repository

import org.library.todo.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * User Repository 인터페이스
 * 사용자 정보에 대한 데이터베이스 접근을 담당
 * 
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공받음
 * - save(), findById(), findAll(), deleteById() 등
 * 
 * @Repository: Spring Data JPA가 자동으로 구현체를 생성
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회
     * 로그인 시 사용
     * 
     * @param email 조회할 사용자의 이메일
     * @return 해당 이메일의 사용자가 있으면 User 객체, 없으면 null
     */
    fun findByEmail(email: String): User?
    
    /**
     * 이메일 존재 여부 확인
     * 회원가입 시 중복 이메일 체크용
     * 
     * @param email 확인할 이메일
     * @return 해당 이메일이 이미 존재하면 true, 없으면 false
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * 닉네임 존재 여부 확인
     * 회원가입 시 중복 닉네임 체크용 (선택사항)
     * 
     * @param nickname 확인할 닉네임
     * @return 해당 닉네임이 이미 존재하면 true, 없으면 false
     */
    fun existsByNickname(nickname: String): Boolean
}
