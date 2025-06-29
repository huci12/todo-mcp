# TODO LIST 바이브 코딩
- Kotlin , thymeleaf , jpa 사용


## 사용 프롬프트


역할
너는 숙련된 백엔드 개발자야. 내가 요청한 기능에 맞게 Kotlin + Spring Boot 기반의 코드를 작성해줘.

기술 스택
Spring Boot 3.x, Spring Web, Spring Data JPA, H2DB



1.생성된 프로젝트에 투두리스트 개발 요청

요구사항
1. Todo Entity는 title, description, isDone 필드를 갖는다.
2. title은 필수이며, description은 선택, isDone은 기본값 false.
3. 투두를 등록(Create), 전체 조회(Read), 단건 조회(Read), 수정(Update), 삭제(Delete)하는 REST API를 만들어줘.
4. 각 기능에 맞는 DTO, Service, Controller를 작성해줘.
5. Repository는 JpaRepository로 만들어줘.
6. 테스트 코드는 나중에 따로 요청할게.

2.Thymeleaf + HTML로 화면 생성
역할
너는 Spring Boot + Thymeleaf를 사용하는 웹 프론트엔드 개발자야.
기존 작성된 서버 코드를 바탕으로 Thymeleaf와 연동을 진행해주면 돼

요구사항
1. todo 목록을 보여주는 list.html을 만들어줘.
2. todo 등록 폼을 갖는 create.html을 만들어줘.
3. 등록 폼에는 title, description 입력창과 등록 버튼이 있어야 해.
4. 목록 페이지에서는 체크박스로 완료 처리(isDone 수정)가 가능해야 해.
5. Controller는 @Controller 애노테이션을 사용하고, Model에 데이터 넣어서 view에 전달해줘.
6. thymeleaf 문법은 기본적인 것만 사용해줘.

3.세션기반 로그인/회원가입 개발
역할
너는 Spring Boot 백엔드 개발자야. 기존에 만든 투두 리스트 서버에 세션 기반 회원가입/로그인 기능을 연동할 거야.
로그인한 사용자만 자신의 투두 목록을 조회, 생성, 수정, 삭제할 수 있게 해줘.

현재 상황
- Todo 엔티티는 이미 존재하고, Todo CRUD 기능도 구현되어 있어.
- Thymeleaf 기반의 프론트엔드 (list.html, create.html) 도 구현되어 있어.

해야 할 일

회원 관련 기능 추가

1. User Entity 생성
   - email, password (BCrypt로 암호화), nickname 필드
   - email은 유일해야 해.

2. 회원가입 기능 (/signup)
   - Thymeleaf로 signup.html 폼 페이지
   - 중복 이메일은 등록 불가
   - 성공 시 /login으로 리다이렉트

3. 로그인 기능 (/login)
   - Thymeleaf login.html 폼 페이지
   - 로그인 성공 시 세션에 사용자 정보 저장
   - 실패 시 오류 메시지 표시

4. 로그아웃 기능 (/logout)
   - 세션 무효화 후 /login으로 이동

투두 기능과 사용자 연동

1. Todo 엔티티에 User (ManyToOne) 연관관계 추가
2. 로그인한 사용자만 자신의 투두를 조회/등록/수정/삭제 가능하게 수정
3. 투두 생성 시 현재 로그인한 사용자를 Todo의 주인으로 설정
4. 컨트롤러에서 세션을 통해 로그인 유저 정보를 가져오고, 해당 유저의 투두만 처리

Thymeleaf 연동

- 로그인된 유저의 닉네임을 list.html에 출력해줘
- 로그인하지 않은 사용자가 /todos 등에 접근하면 /login으로 리다이렉트

그외 공통적인 예외 처리 클래스에 대한 처리 및 화면 깨짐 현상에 대한 부분은 직접 코드 작성
