-- Teacher 권한을 가진 테스트 사용자 생성 SQL
-- createClass 기능 테스트를 위한 데이터

INSERT INTO users (username, password, name, role, status, latitude, longitude, created_at, updated_at)
VALUES (
    'teacher_test',                    -- username (unique)
    '$2a$10$dummy.password.hash',      -- password (BCrypt 해시 예시)
    '테스트 선생님',                     -- name
    'TEACHER',                         -- role (TEACHER 권한)
    'ACTIVE',                          -- status
    37.5665,                          -- latitude (서울 예시 좌표)
    126.9780,                         -- longitude (서울 예시 좌표)
    NOW(),                            -- created_at
    NOW()                             -- updated_at
);

-- 생성된 사용자 확인
SELECT user_id, username, name, role, status 
FROM users 
WHERE username = 'teacher_test';