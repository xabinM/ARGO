# 카드 게임 API 문서

## 개요
Argo 앱의 카드 배틀 시스템을 위한 백엔드 API 명세서입니다. 
서버 DB 구조(team_cards 테이블)와 Android 클라이언트 매퍼를 기반으로 작성되었습니다.

## 데이터 구조 개요
- **서버**: `team_cards` 테이블로 팀 카드 소유 정보 관리
- **클라이언트**: 매퍼를 통해 cardId로 카드 기본 정보(이름, 능력치) 계산

## team_cards 테이블 구조
```sql
Table team_cards {
  team_card_id bigint [pk, increment]
  team_id bigint [not null]
  card_id bigint [not null]
  tier enum('COMMON', 'EPIC', 'RARE', 'LEGENDARY') [not null]
  obtained_at datetime(6) [default: 'CURRENT_TIMESTAMP(6)']
  is_lost boolean [default: false, note: '게임에서 잃은 카드']
  is_locked boolean [default: false, note: '게임 중 잠금 상태']
  created_at datetime(6) [default: 'CURRENT_TIMESTAMP(6)']
}
```

## 게임 규칙
- **공격 vs 방어**: 높은 능력치(공격력+방어력)가 승리
  - 승리 시: 공격=100점, 방어=50점
- **공격 vs 공격**: 무승부 → 양쪽 카드 `is_lost = true` + 100점씩 획득
- **방어 vs 방어**: 양쪽 카드 `is_lost = true` + 50점씩 획득  
- **패배 시**: 공격 선택 시 카드 `is_lost = true`, 방어 선택 시 패스

---

## 1. 카드 관리 API

### 1.1 카드 기본 정보 조회 (클라이언트 매퍼 참고용)
```http
GET /api/cards/{cardId}
```

**설명**: 클라이언트에서는 이미 매퍼로 처리하므로, 서버에서는 cardId 유효성 검증 목적으로만 사용

**응답**
```json
{
  "success": true,
  "data": {
    "cardId": 1,
    "exists": true
  }
}
```

**클라이언트 처리**: 
- `GameCard.create(cardId, tier)` 매퍼 함수 사용
- 이름, 공격력, 방어력, 설명은 클라이언트에서 계산

---

## 2. 팀 카드 컬렉션 API

### 2.1 팀 카드 컬렉션 조회 (전체)
```http
GET /api/teams/{teamId}/cards
```

**응답** - `team_cards` 테이블의 모든 카드 (활성/잃어버린/잠긴 카드 포함)
```json
{
  "success": true,
  "data": {
    "teamId": 1,
    "teamCards": [
      {
        "teamCardId": 101,
        "cardId": 1,
        "tier": "LEGENDARY",
        "obtainedAt": "2024-08-01T10:00:00Z",
        "isLost": false,
        "isLocked": false
      },
      {
        "teamCardId": 102,
        "cardId": 2,
        "tier": "EPIC", 
        "obtainedAt": "2024-08-02T15:30:00Z",
        "isLost": false,
        "isLocked": true
      },
      {
        "teamCardId": 99,
        "cardId": 3,
        "tier": "RARE",
        "obtainedAt": "2024-08-01T09:00:00Z",
        "isLost": true,
        "isLocked": false
      }
    ],
    "totalCount": 3,
    "tierStats": {
      "COMMON": 0,
      "RARE": 1,
      "EPIC": 1,
      "LEGEND": 1
    }
  }
}
```

**클라이언트 필터링 예시**:
```kotlin
// 전체 카드를 GameCard로 변환
val allGameCards = teamCards.map { teamCard ->
    GameCard.create(teamCard.cardId, teamCard.tier)
}

// 클라이언트에서 상태별 필터링
val availableCards = teamCards.filter { !it.isLost && !it.isLocked }
val lostCards = teamCards.filter { it.isLost }
val lockedCards = teamCards.filter { it.isLocked }

// UI에 따라 적절히 필터링하여 표시
val displayCards = when (selectedFilter) {
    "ALL" -> allGameCards
    "AVAILABLE" -> availableCards.map { GameCard.create(it.cardId, it.tier) }
    "LOST" -> lostCards.map { GameCard.create(it.cardId, it.tier) }
}
```

---

## 3. 대전 관리 API

### 3.1 대전 가능한 팀 목록 조회
```http
GET /api/teams/{teamId}/battle-opponents
```

**응답** - `teams` 테이블 기반 (필수 정보만)
```json
{
  "success": true,
  "data": [
    {
      "teamId": 2,
      "teamName": "불사조 팀",
      "leaderName": "김철수",
      "totalGames": 25,
      "wins": 18,
      "losses": 5,
      "draws": 2,
      "totalPoints": 2100
    }
  ]
}
```

**클라이언트 계산**:
```kotlin
data class BattleTeam(
    val teamId: Long,
    val teamName: String,
    val leaderName: String,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val totalPoints: Int
) {
    val winRate: Double
        get() = if (totalGames > 0) wins.toDouble() / totalGames * 100 else 0.0
        
    val drawRate: Double
        get() = if (totalGames > 0) draws.toDouble() / totalGames * 100 else 0.0
        
    val averagePointsPerGame: Double
        get() = if (totalGames > 0) totalPoints.toDouble() / totalGames else 0.0
}
```

### 3.2 대전 신청
```http
POST /api/battles
```

**요청**
```json
{
  "challengerTeamId": 1,
  "challengedTeamId": 2,
  "selectedCard": {
    "teamCardId": 101,
    "battleStance": "ATTACK"
  }
}
```

**처리 과정**:
1. `teamCardId`로 `team_cards` 테이블에서 `cardId`, `tier` 조회
2. 소유권 및 카드 상태(`isLost`, `isLocked`) 검증
3. `is_locked = true` 설정 (대전 진행 중)
4. 대전 신청 생성

**응답**
```json
{
  "success": true,
  "message": "대전 신청이 성공적으로 전송되었습니다"
}
```

### 3.3 대전 응답 (수락/거절)
```http
PUT /api/battles/{matchId}/respond
```

**요청**
```json
{
  "action": "ACCEPT",
  "selectedCard": {
    "teamCardId": 105,
    "battleStance": "DEFENSE"
  }
}
```

**응답**
```json
{
  "success": true,
  "message": "대전 수락이 완료되었습니다"
}
```

**참고**: 대전 결과는 별도 API(`GET /api/teams/{teamId}/battle-history`)로 조회

**클라이언트 처리**:
```kotlin
// 1. 대전 수락 후
battleApi.acceptBattle(matchId, selectedCard) 

// 2. 결과 조회 (BattleHistory에서 teamCardId, cardId, tier 포함)
val battleHistory = battleApi.getBattleHistory(teamId)

// 3. GameCard 생성 (클라이언트 매퍼 사용)
val myGameCard = GameCard.create(battle.myCard.cardId, battle.myCard.tier)
val opponentGameCard = GameCard.create(battle.opponentCard.cardId, battle.opponentCard.tier)
```

### 3.4 대전 신청 취소
```http
DELETE /api/battles/{matchId}
```

**응답**
```json
{
  "success": true,
  "message": "대전 신청이 취소되었습니다"
}
```

---

## 4. 대전 기록 API

### 4.1 팀 대전 기록 조회
```http
GET /api/teams/{teamId}/battle-history?page=0&size=10
```

**응답**
```json
{
  "success": true,
  "data": {
    "battles": [
      {
        "matchId": 123,
        "challengerTeamId": 1,
        "challengedTeamId": 2,
        "challengerTeamName": "드래곤 슬레이어",
        "challengedTeamName": "불사조 팀",
        "status": "COMPLETED",
        "resultView": "BOTH_NOT_SEE",
        "winnerTeamId": 1,
        "loserTeamId": 2,
        "isDraw": false,
        "myCard": {
          "teamCardId": 101,
          "cardId": 1,
          "tier": "LEGEND",
          "battleStance": "ATTACK"
        },
        "opponentCard": {
          "teamCardId": 105,
          "cardId": 3,
          "tier": "RARE",
          "battleStance": "DEFENSE"
        },
        "createdAt": "2024-08-06T10:00:00Z",
        "endedAt": "2024-08-06T10:05:00Z"
      },
      {
        "matchId": 124,
        "challengerTeamId": 3,
        "challengedTeamId": 1,
        "challengerTeamName": "그리핀 팀",
        "challengedTeamName": "드래곤 슬레이어",
        "status": "COMPLETED",
        "resultView": "BOTH_SEE",
        "winnerTeamId": null,
        "loserTeamId": null,
        "isDraw": true,
        "myCard": {
          "teamCardId": 102,
          "cardId": 2,
          "tier": "EPIC",
          "battleStance": "ATTACK"
        },
        "opponentCard": {
          "teamCardId": 106,
          "cardId": 4,
          "tier": "EPIC",
          "battleStance": "ATTACK"
        },
        "createdAt": "2024-08-05T14:00:00Z",
        "endedAt": "2024-08-05T14:05:00Z"
      }
    ],
    "totalElements": 15,
    "totalPages": 2,
    "currentPage": 0
  }
}
```

### 4.2 대전 결과 확인
```http
PUT /api/battles/{matchId}/view-result
```

**요청**: 빈 요청 (사용자 ID는 토큰에서 추출)

**응답**
```json
{
  "success": true,
  "message": "대전 결과 확인이 처리되었습니다"
}
```

**처리 과정**:
1. `battles` 테이블의 `resultView` 상태 업데이트
2. 클라이언트는 이미 받은 `battle-history` 데이터로 결과 화면 표시

---

## 5. 팀 통계 API

### 5.1 팀 카드 게임 통계 조회
```http
GET /api/teams/{teamId}/stats
```

**응답** - `teams` 테이블 기반 (필수 정보만)
```json
{
  "success": true,
  "data": {
    "teamId": 1,
    "teamName": "드래곤 슬레이어",
    "leaderName": "박영희",
    "totalGames": 17,
    "wins": 12,
    "losses": 3,
    "draws": 2,
    "totalPoints": 2450,
    "createdAt": "2024-07-15T10:00:00Z"
  }
}
```

**클라이언트에서 계산**:
```kotlin
// 승률 계산
val winRate = if (totalGames > 0) wins.toDouble() / totalGames * 100 else 0.0

// 무승부율 계산  
val drawRate = if (totalGames > 0) draws.toDouble() / totalGames * 100 else 0.0

// 게임당 평균 점수
val averageScore = if (totalGames > 0) totalPoints.toDouble() / totalGames else 0.0
```
