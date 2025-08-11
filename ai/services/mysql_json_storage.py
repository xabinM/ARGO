# services/mysql_quiz_storage.py - MySQL JSON 저장 방식
"""
🗄️ MySQL JSON 저장 방식 (가장 실무적)

장점:
1. 구현 간단 (30분 내 완성)
2. 백엔드에서 일반적인 DB 조회로 접근
3. JSON 컬럼으로 유연한 데이터 구조
4. 대량 배치 업로드 최적화
"""

import asyncio
import aiomysql
import json
import logging
from typing import List, Dict, Optional
from datetime import datetime
import os

logger = logging.getLogger(__name__)

class MySQLQuizStorage:
    """MySQL JSON 퀴즈 저장 서비스"""
    
    def __init__(self):
        self.pool = None
        self.db_config = {
            'host': os.getenv('MYSQL_HOST', 'localhost'),
            'port': int(os.getenv('MYSQL_PORT', '3306')),
            'user': os.getenv('MYSQL_USER', 'root'),
            'password': os.getenv('MYSQL_PASSWORD', ''),
            'db': os.getenv('MYSQL_DATABASE', 'argo_db'),
            'charset': 'utf8mb4',
            'autocommit': True
        }
    
    async def initialize(self):
        """MySQL 연결 풀 초기화"""
        try:
            self.pool = await aiomysql.create_pool(**self.db_config)
            
            # 테이블 생성
            await self._create_tables()
            
            logger.info("✅ MySQL 퀴즈 저장소 초기화 완료")
            return True
            
        except Exception as e:
            logger.error(f"❌ MySQL 연결 실패: {e}")
            return False
    
    async def _create_tables(self):
        """퀴즈 저장용 테이블 생성"""
        async with self.pool.acquire() as conn:
            async with conn.cursor() as cursor:
                
                # 퀴즈 배치 테이블 (메타정보)
                await cursor.execute("""
                CREATE TABLE IF NOT EXISTS quiz_batches (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    spot_name VARCHAR(255) NOT NULL,
                    location VARCHAR(255) NOT NULL,
                    grade INT NOT NULL,
                    total_problems INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_spot_grade (spot_name, grade),
                    INDEX idx_location_grade (location, grade)
                )
                """)
                
                # 퀴즈 문제 테이블 (JSON 저장)
                await cursor.execute("""
                CREATE TABLE IF NOT EXISTS quiz_problems_json (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    batch_id INT NOT NULL,
                    spot_name VARCHAR(255) NOT NULL,
                    grade INT NOT NULL,
                    problem_data JSON NOT NULL,
                    quality_score DECIMAL(3,2) DEFAULT 0.00,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (batch_id) REFERENCES quiz_batches(id) ON DELETE CASCADE,
                    INDEX idx_spot_grade (spot_name, grade),
                    INDEX idx_quality (quality_score),
                    INDEX idx_batch (batch_id)
                )
                """)
                
                await conn.commit()
                logger.info("📊 퀴즈 테이블 준비 완료")
    
    async def save_quiz_batch(
        self, 
        spot_name: str, 
        location: str, 
        grade: int,
        quiz_problems: List[Dict],
        spot_metadata: Optional[Dict] = None
    ) -> Dict:
        """퀴즈 배치 저장"""
        
        if not self.pool:
            return {"success": False, "error": "DB 연결이 없습니다"}
        
        async with self.pool.acquire() as conn:
            async with conn.cursor() as cursor:
                try:
                    # 1. 배치 정보 저장
                    await cursor.execute("""
                    INSERT INTO quiz_batches (spot_name, location, grade, total_problems)
                    VALUES (%s, %s, %s, %s)
                    """, (spot_name, location, grade, len(quiz_problems)))
                    
                    batch_id = cursor.lastrowid
                    
                    # 2. 각 퀴즈 문제를 JSON으로 저장
                    saved_count = 0
                    skipped_count = 0
                    
                    for problem in quiz_problems:
                        # 품질 체크
                        quality_score = problem.get("quality_score", 0.0)
                        if quality_score < 0.5:
                            skipped_count += 1
                            continue
                        
                        # JSON 데이터 준비
                        problem_json = {
                            "question": problem["question"],
                            "choices": problem["choices"],
                            "correctIndex": problem["correctIndex"],
                            "explanation": problem.get("explanation", ""),
                            "generation_method": problem.get("generation_method", "unknown"),
                            "parsing_method": problem.get("parsing_method", "unknown"),
                            "spot_metadata": spot_metadata or {}
                        }
                        
                        # DB에 저장
                        await cursor.execute("""
                        INSERT INTO quiz_problems_json 
                        (batch_id, spot_name, grade, problem_data, quality_score)
                        VALUES (%s, %s, %s, %s, %s)
                        """, (
                            batch_id,
                            spot_name,
                            grade,
                            json.dumps(problem_json, ensure_ascii=False),
                            quality_score
                        ))
                        
                        saved_count += 1
                    
                    # 3. 배치 정보 업데이트
                    await cursor.execute("""
                    UPDATE quiz_batches SET total_problems = %s WHERE id = %s
                    """, (saved_count, batch_id))
                    
                    await conn.commit()
                    
                    result = {
                        "success": True,
                        "batch_id": batch_id,
                        "spot_name": spot_name,
                        "location": location,
                        "grade": grade,
                        "saved_problems": saved_count,
                        "skipped_problems": skipped_count,
                        "total_problems": len(quiz_problems)
                    }
                    
                    logger.info(f"💾 MySQL 저장 완료: {spot_name} - {saved_count}개 저장")
                    return result
                    
                except Exception as e:
                    await conn.rollback()
                    logger.error(f"❌ MySQL 저장 실패: {e}")
                    return {"success": False, "error": str(e)}
    
    async def get_quiz_problems(
        self, 
        spot_name: Optional[str] = None,
        location: Optional[str] = None,
        grade: Optional[int] = None,
        limit: int = 50
    ) -> List[Dict]:
        """퀴즈 문제 조회 (백엔드에서 사용할 메소드)"""
        
        if not self.pool:
            return []
        
        async with self.pool.acquire() as conn:
            async with conn.cursor(aiomysql.DictCursor) as cursor:
                try:
                    # 동적 쿼리 구성
                    where_conditions = []
                    params = []
                    
                    if spot_name:
                        where_conditions.append("spot_name = %s")
                        params.append(spot_name)
                    
                    if location:
                        where_conditions.append("JSON_UNQUOTE(JSON_EXTRACT(problem_data, '$.spot_metadata.location')) LIKE %s")
                        params.append(f"%{location}%")
                    
                    if grade:
                        where_conditions.append("grade = %s")
                        params.append(grade)
                    
                    where_clause = " AND ".join(where_conditions) if where_conditions else "1=1"
                    params.append(limit)
                    
                    query = f"""
                    SELECT 
                        id,
                        spot_name,
                        grade,
                        problem_data,
                        quality_score,
                        created_at
                    FROM quiz_problems_json 
                    WHERE {where_clause}
                    ORDER BY quality_score DESC, created_at DESC
                    LIMIT %s
                    """
                    
                    await cursor.execute(query, params)
                    rows = await cursor.fetchall()
                    
                    # JSON 데이터 파싱
                    results = []
                    for row in rows:
                        problem_data = json.loads(row['problem_data'])
                        result = {
                            "id": row['id'],
                            "spot_name": row['spot_name'],
                            "grade": row['grade'],
                            "quality_score": float(row['quality_score']),
                            "created_at": row['created_at'].isoformat(),
                            **problem_data  # JSON 데이터 펼치기
                        }
                        results.append(result)
                    
                    return results
                    
                except Exception as e:
                    logger.error(f"❌ 퀴즈 조회 실패: {e}")
                    return []
    
    async def get_quiz_statistics(self) -> Dict:
        """퀴즈 통계 조회"""
        
        if not self.pool:
            return {}
        
        async with self.pool.acquire() as conn:
            async with conn.cursor(aiomysql.DictCursor) as cursor:
                try:
                    # 전체 통계
                    await cursor.execute("""
                    SELECT 
                        COUNT(*) as total_problems,
                        COUNT(DISTINCT spot_name) as total_spots,
                        COUNT(DISTINCT grade) as total_grades,
                        AVG(quality_score) as avg_quality,
                        MIN(created_at) as first_created,
                        MAX(created_at) as last_created
                    FROM quiz_problems_json
                    """)
                    
                    overall_stats = await cursor.fetchone()
                    
                    # 학년별 통계
                    await cursor.execute("""
                    SELECT grade, COUNT(*) as count
                    FROM quiz_problems_json
                    GROUP BY grade
                    ORDER BY grade
                    """)
                    
                    grade_stats = {row['grade']: row['count'] for row in await cursor.fetchall()}
                    
                    # 스팟별 상위 10개
                    await cursor.execute("""
                    SELECT spot_name, COUNT(*) as count
                    FROM quiz_problems_json
                    GROUP BY spot_name
                    ORDER BY count DESC
                    LIMIT 10
                    """)
                    
                    top_spots = await cursor.fetchall()
                    
                    return {
                        "overall": overall_stats,
                        "by_grade": grade_stats,
                        "top_spots": top_spots
                    }
                    
                except Exception as e:
                    logger.error(f"❌ 통계 조회 실패: {e}")
                    return {}
    
    async def cleanup(self):
        """연결 풀 정리"""
        if self.pool:
            self.pool.close()
            await self.pool.wait_closed()

# === 글로벌 인스턴스 ===
mysql_storage = None

async def get_mysql_storage():
    """MySQL 저장소 싱글톤"""
    global mysql_storage
    if mysql_storage is None:
        mysql_storage = MySQLQuizStorage()
        await mysql_storage.initialize()
    return mysql_storage

# === 편의 함수 ===
async def save_to_mysql(spot_name: str, location: str, grade: int, problems: List[Dict]) -> Dict:
    """MySQL에 퀴즈 저장"""
    storage = await get_mysql_storage()
    return await storage.save_quiz_batch(spot_name, location, grade, problems)

async def get_quizzes_from_mysql(spot_name: str = None, grade: int = None) -> List[Dict]:
    """MySQL에서 퀴즈 조회"""
    storage = await get_mysql_storage()
    return await storage.get_quiz_problems(spot_name=spot_name, grade=grade)
