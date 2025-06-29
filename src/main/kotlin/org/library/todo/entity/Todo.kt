package org.library.todo.entity

import jakarta.persistence.*

/**
 * Todo 엔티티 클래스
 * 할 일 정보를 데이터베이스에 저장하기 위한 JPA 엔티티
 */
@Entity
@Table(name = "todos")
data class Todo(
    /**
     * 기본키 ID
     * 자동 증가하는 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    
    /**
     * 할 일 제목
     * 필수 입력 필드 (NOT NULL)
     */
    @Column(nullable = false)
    val title: String,
    
    /**
     * 할 일 상세 설명
     * 선택 입력 필드 (NULL 허용)
     * TEXT 타입으로 긴 텍스트 저장 가능
     */
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    /**
     * 완료 여부
     * 기본값은 false (미완료 상태)
     * NOT NULL 제약 조건
     */
    @Column(nullable = false)
    val isDone: Boolean = false,
    
    /**
     * Todo 작성자 (사용자)
     * 다대일 관계: 한 사용자가 여러 Todo를 가질 수 있음
     * 지연 로딩으로 설정
     * null 불가 (모든 Todo는 작성자가 있어야 함)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) {
    /**
     * JPA를 위한 기본 생성자
     * 프록시 객체 생성 시 필요
     */
    constructor() : this(0L, "", null, false, User())
    
    /**
     * 연관관계 필드 제외 toString
     * 무한 순환 참조 방지
     */
    override fun toString(): String {
        return "Todo(id=$id, title='$title', description=$description, isDone=$isDone, userId=${user.id})"
    }
    
    /**
     * equals와 hashCode는 id 기준으로 재정의
     * JPA 엔티티의 동등성 비교를 위함
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Todo
        
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
}
