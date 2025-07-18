# 현장체험학습 프로그램 API 명세서

## 📋 개요

이 프로그램은 AR(증강현실) 기반의 현장체험학습 플랫폼으로, 선생님과 학생이 함께 참여하는 교육용 게임 시스템입니다.

### 주요 기능
- **반 관리**: 선생님이 반을 생성하고 학생들을 관리
- **팀 시스템**: 학생들을 팀으로 구성하여 협력 학습
- **AR 미션**: 현장에서 AR 오브젝트를 통한 미션 수행
- **카드 수집**: 미션 완료 시 랜덤 카드 보상
- **팀 배틀**: 수집한 카드로 팀 간 대전
- **실시간 모니터링**: 교사용 진행 현황 추적

---

## 🔐 인증

모든 API는 JWT(JSON Web Token) 기반 인증을 사용합니다.

### 헤더 형식
```
Authorization: Bearer {JWT_TOKEN}
```

### 사용자 권한
- **teacher**: 교사 권한 (반 생성, 관리)
- **student**: 학생 권한 (반 참여, 미션 수행)

---

## 📚 반(클래스) 관리 API

### 1. 반 생성 (교사)
```http
POST /api/classes
```

**Request Body:**
```json
{
  "className": "6학년 1반 봄 소풍",
  "description": "2025년 봄 소풍 현장체험학습",
  "location": "서울대공원",
  "activityDate": "2025-04-15",
  "maxStudents": 25
}
```

**Response:**
```json
{
  "success": true,
  "message": "반이 생성되었습니다.",
  "data": {
    "classId": 1001,
    "className": "6학년 1반 봄 소풍",
    "inviteCode": "ABC123",
    "createdAt": "2025-07-16T14:30:00Z"
  }
}
```

### 2. 반 목록 조회 (교사)
```http
GET /api/classes?status=active&page=1&size=10
```

### 3. 반 상세 조회 (공통)
```http
GET /api/classes/{classId}
```

### 4. 반 삭제 (교사)
```http
DELETE /api/classes/{classId}
```

---

## 👥 팀 관리 API

### 1. 팀 생성 (교사)
```http
POST /api/classes/{classId}/teams
```

**Request Body:**
```json
{
  "teamName": "팀 A",
  "maxMembers": 5
}
```

### 2. 팀에 학생 배정 (교사)
```http
POST /api/classes/{classId}/teams/{teamId}/assign
```

**Request Body:**
```json
{
  "studentIds": [2001, 2002, 2003]
}
```

### 3. 랜덤 팀 배정 (교사)
```http
POST /api/classes/{classId}/teams/random-assign
```

**Request Body:**
```json
{
  "assignmentType": "balanced"
}
```

### 4. 팀 삭제 (교사)
```http
DELETE /api/classes/{classId}/teams/{teamId}
```

---

## 🎓 학생 참여 API

### 1. 반 참여 신청 (학생)
```http
POST /api/classes/join
```

**Request Body:**
```json
{
  "inviteCode": "ABC123"
}
```

### 2. 내 신청 현황 조회 (학생)
```http
GET /api/my/applications?status=all&page=1&size=10
```

### 3. 반 탈퇴 (학생)
```http
DELETE /api/classes/{classId}/leave
```

### 4. 참여 신청 목록 조회 (교사)
```http
GET /api/classes/{classId}/applications?status=pending
```

### 5. 참여 신청 승인/거절 (교사)
```http
PUT /api/classes/{classId}/applications/{applicationId}
```

**Request Body:**
```json
{
  "action": "approve"
}
```

---

## 🎯 미션 시스템 API

### 1. 미션 스팟 추가 (교사)
```http
POST /api/teachers/classes/{classId}/mission-spots
```

**Request Body:**
```json
{
  "latitude": 37.4270,
  "longitude": 126.9915,
  "address": "서울대공원 동물원 입구",
  "landmark": "동물원 안내 센터 앞",
  "radius": 50,
  "spotName": "동물원 입구 첫 미션",
  "description": "동물원 입구에서 첫 번째 미션을 시작합니다.",
  "isSequential": false,
  "missions": [
    {
      "title": "동물원 입구 인증샷",
      "type": "photo_verify",
      "rewardAmount": 70,
      "challengeDetails": {
        "instruction": "동물원 입구 간판과 함께 팀원들이 모두 나오도록 사진을 찍어주세요."
      }
    }
  ]
}
```

### 2. 미션 시작 (학생)
```http
POST /api/classes/{classId}/teams/{teamId}/missions/{spotId}/start
```

**Response:**
```json
{
  "success": true,
  "message": "미션이 시작되었습니다.",
  "data": {
    "missionSession": {
      "sessionId": "session_abc123",
      "spotId": 2001,
      "teamId": 101,
      "startedAt": "2025-04-15T10:00:00Z",
      "expiresAt": "2025-04-15T10:30:00Z"
    },
    "mission": {
      "missionType": "qr_scan",
      "title": "QR 코드를 찾아 스캔하세요",
      "description": "메인 입구 주변에 숨겨진 QR 코드를 찾아서 스캔해주세요.",
      "instructions": [
        "AR 카메라를 통해 주변을 살펴보세요",
        "QR 코드가 나타나면 화면을 터치하여 스캔하세요"
      ]
    },
    "arObjects": [
      {
        "objectId": "qr_001",
        "type": "qr_code",
        "position": {"x": 0.5, "y": 1.2, "z": -2.0},
        "modelPath": "/models/qr_code.glb",
        "isInteractable": true
      }
    ]
  }
}
```

### 3. 미션 제출 (학생)
```http
POST /api/students/missions/{missionId}/complete
```

**Request Body (사진 인증):**
```json
{
  "resultType": "photo_verify",
  "photoUrl": "https://cdn.example.com/photo.jpg",
  "comment": "팀원 모두가 함께 찍었어요!"
}
```

**Request Body (퀴즈):**
```json
{
  "resultType": "quiz",
  "answers": [
    {
      "questionId": "q_001",
      "submittedAnswer": "기린"
    }
  ]
}
```

### 4. 전체 미션 위치 조회 (공통)
```http
GET /api/classes/{classId}/missions/spots
```

### 5. 반 전체 미션 진행 현황 조회 (공통)
```http
GET /api/classes/{classId}/missions/teams/overview
```

### 6. 팀별 미션 상세 조회 (공통)
```http
GET /api/classes/{classId}/teams/{teamId}/missions/detail
```

---

## 🃏 카드 시스템 API

### 1. 팀 보유 카드 조회
```http
GET /api/classes/{classId}/teams/{teamId}/cards?sort=rarity&rarity=all&page=1&size=20
```

**Response:**
```json
{
  "success": true,
  "message": "팀 보유 카드 조회 성공",
  "data": {
    "teamInfo": {
      "teamId": 101,
      "teamName": "팀 A"
    },
    "cards": [
      {
        "cardId": 1001,
        "cardName": "용감한 사자",
        "cardImage": "/images/cards/lion_001.png",
        "borderDesign": "gold",
        "attackPower": 85,
        "defensePower": 70,
        "rarity": "legendary",
        "obtainedAt": "2025-07-17T10:30:00Z",
        "obtainedFrom": "미션: 서울대공원 정문"
      }
    ],
    "statistics": {
      "totalCards": 8,
      "cardsByRarity": {
        "legendary": 1,
        "epic": 2,
        "rare": 3,
        "common": 2
      },
      "averageAttack": 75.5,
      "averageDefense": 68.2
    }
  }
}
```

---

## ⚔️ 배틀 시스템 API

### 1. 배틀 요청 (학생)
```http
POST /api/classes/{classId}/teams/{teamId}/battle/request
```

**Request Body:**
```json
{
  "targetTeamId": 102,
  "message": "같이 배틀해요!"
}
```

### 2. 배틀 요청 응답 (학생)
```http
PUT /api/classes/{classId}/teams/{teamId}/battle/requests/{requestId}/respond
```

**Request Body:**
```json
{
  "action": "accept"
}
```

### 3. 배틀 카드 선택 (학생)
```http
POST /api/classes/{classId}/teams/{teamId}/battles/{battleId}/select-card
```

**Request Body:**
```json
{
  "cardId": 1001
}
```

**Response:**
```json
{
  "success": true,
  "message": "카드가 선택되었습니다.",
  "data": {
    "battleId": "battle_001",
    "selectedCard": {
      "cardId": 1001,
      "cardName": "용감한 사자",
      "attackPower": 85,
      "defensePower": 70,
      "rarity": "legendary"
    },
    "battleStatus": "waiting_opponent",
    "timeRemaining": 420
  }
}
```

---

## 📊 통계 및 모니터링 API

### 1. 스팟별 미션 응답 조회 (공통)
```http
GET /api/classes/{classId}/spots/{spotId}/missions/responses
```

### 2. 교사 푸시 알림 전송 (교사)
```http
POST /api/teachers/classes/{classId}/notifications
```

### 3. 스팟별 미션 응답 통계 조회 (교사)
```http
GET /api/teachers/classes/{classId}/spots/{spotId}/statistics
```

---

## 📄 공통 응답 형식

### 성공 응답
```json
{
  "success": true,
  "message": "작업이 성공적으로 완료되었습니다.",
  "data": {
    // 응답 데이터
  }
}
```

### 오류 응답
```json
{
  "success": false,
  "message": "오류 메시지"
}
```

---

## 🚫 HTTP 상태 코드

- **200 OK**: 요청 성공
- **201 Created**: 리소스 생성 성공
- **400 Bad Request**: 잘못된 요청
- **401 Unauthorized**: 인증 필요
- **403 Forbidden**: 권한 부족
- **404 Not Found**: 리소스 없음
- **500 Internal Server Error**: 서버 오류

---

## 🔒 권한 매트릭스

| API 그룹 | 교사 | 학생 |
|----------|------|------|
| 반 생성/삭제 | ✅ | ❌ |
| 팀 관리 | ✅ | ❌ |
| 반 참여 신청 | ❌ | ✅ |
| 미션 수행 | ❌ | ✅ |
| 카드 배틀 | ❌ | ✅ |
| 진행 현황 조회 | ✅ | ✅ (제한적) |

---

## 📝 참고사항

1. **JWT 토큰**: 모든 요청에 Authorization 헤더 필요
2. **권한 검증**: 각 API마다 적절한 권한 확인
3. **데이터 유효성**: 입력값 검증 및 오류 처리
4. **실시간 기능**: WebSocket 또는 Server-Sent Events 활용 권장
5. **파일 업로드**: 사진 업로드는 별도 CDN 서비스 활용
6. **지리적 위치**: GPS 좌표 기반 미션 스팟 인증

---