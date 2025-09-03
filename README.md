# 2025_SEASONTHON_TEAM_8_BE
# 🚀 프로젝트 규칙 및 컨벤션

## ✅ 1. 브랜치 전략

### 📦 브랜치 규칙
- `main`: 배포 가능한 안정화 버전만
- `develop`: 다음 배포를 위한 통합 브랜치
- `feature/*`: 기능 단위 작업용 브랜치
- `hotfix/*`: 긴급 수정
- `release/*`: 배포 준비 브랜치

### 📦 Github Flow (단순)
- `main`: 항상 최신
- 작업마다 `feature/*` 브랜치 → PR → merge

## ✅ 2. 코드 컨벤션

### 📦 패키지 구조
- 패키지
    - api
        - controller
        - dto
    - application
        - service
    - domain
        - entity
        - repository

### 📦 Service 구조
- 인터페이스: `SomethingService`
- 구현체: `SomethingServiceImpl`

### 📦 에러 관리
- 공통 에러코드 파일 사용

## ✅ 3. PR 규칙

- **PR 제목 규칙**
  - [Feat] 회원가입 API 구현
  - [Fix] 로그인 버그 수정
  - [Refactor] 코드 리팩토링
  - [Chore] .gitignore 수정

- 리뷰어 지정 필수
- Merge 방식: **Squash merge**
- Merge는 반드시 팀원과 같이 확인 후 진행

## ✅ 4. 템플릿

### 📦 ISSUE Template
```markdown
# 📌 Summary
이슈의 간단한 설명

# 🪧 Description
문제, 제안 기능 자세히 작성

# 🪜 Steps to Reproduce
(버그 리포트일 경우)
1. 첫 번째 단계
2. 두 번째 단계
3. 오류 발생

# ✅ Expected Behavior
예상한 동작 설명

# 📎 Related Issues
관련된 이슈 번호 (예: #12)
```

### 📦 PULL REQUEST Template
```markdown
# 📌 Summary
간단한 설명 작성

# ✅ Changes
- 변경사항1
- 변경사항2

# 🚨 Test
- [ ] 테스트1
- [ ] 테스트2

# 📎 Related Issue
Closes #이슈번호
```

