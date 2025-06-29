package org.library.todo.entity

import jakarta.persistence.*

/**
 * 사용자 엔티티
 * 
 * @Entity: JPA 엔티티로 지정
 * @Table: 테이블명을 "users"로 지정 (user는 예약어일 수 있음)
 */
@Entity
@Table(name = "users")
data class User(
    /**
     * 사용자 고유 식별자
     * 자동 증가되는 Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    /**
     * 사용자 이메일 (로그인 ID)
     * 유일해야 함 (중복 불가)
     * null 불가
     */
    @Column(unique = true, nullable = false)
    val email: String,
    
    /**
     * 사용자 비밀번호 (BCrypt로 암호화됨)
     * null 불가
     */
    @Column(nullable = false)
    val password: String,
    
    /**
     * 사용자 닉네임
     * null 불가
     */
    @Column(nullable = false)
    val nickname: String,
    
    /**
     * 사용자가 작성한 Todo 목록
     * 지연 로딩으로 설정
     * 사용자 삭제 시 Todo도 함께 삭제 (CASCADE)
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val todos: MutableList<Todo> = mutableListOf()
) {
    /**
     * JPA를 위한 기본 생성자
     * data class의 특성상 명시적으로 정의
     */
    constructor() : this(0, "", "", "")
    
    /**
     * 비밀번호 제외 toString
     * 보안상 비밀번호는 로그에 출력하지 않음
     */
    override fun toString(): String {
        return "User(id=$id, email='$email', nickname='$nickname')"
    }
    
    /**
     * equals와 hashCode는 id 기준으로 재정의
     * JPA 엔티티의 동등성 비교를 위함
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as User
        
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
}
