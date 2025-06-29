package org.library

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Todo 애플리케이션의 메인 클래스
 * Spring Boot 애플리케이션의 진입점
 * 
 * @SpringBootApplication: 다음 어노테이션들을 통합한 메타 어노테이션
 * - @Configuration: 설정 클래스임을 나타냄
 * - @EnableAutoConfiguration: Spring Boot의 자동 구성 활성화
 * - @ComponentScan: 컴포넌트 스캔 활성화 (현재 패키지 및 하위 패키지)
 */
@SpringBootApplication
class TodoApplication

/**
 * 애플리케이션 시작점
 * JVM이 실행하는 main 함수
 * 
 * @param args 명령행 인수
 */
fun main(args: Array<String>) {
    // Spring Boot 애플리케이션 실행
    // runApplication: Kotlin용 Spring Boot 실행 함수
    runApplication<TodoApplication>(*args)
}
