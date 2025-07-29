package com.example.bogoargo.data.repository

import com.example.bogoargo.data.model.*
import kotlinx.coroutines.delay

class ClassRepository {
    
    // 임시 데이터 - 실제 구현에서는 API 호출로 대체
    private val mockClasses = mutableListOf<Class>(
        Class(
            id = "class_1",
            year = 2025,
            school = "싸피 초등학교",
            className = "1학년 1반",
            description = "우리 반은 체험 학습을 통해 다양한 경험을 쌓고 있습니다.",
            region = "서울",
            invitationCode = "ABC12DEF",
            maxStudents = 30,
            currentStudents = 25,
            schoolName = "싸피 초등학교",
        ),
        Class(
            id = "class_2",
            year = 2025,
            school = "싸피 초등학교",
            className = "2학년 1반",
            description = "즐겁게 배우는 우리 반입니다.",
            region = "서울",
            invitationCode = "XYZ98GHI",
            maxStudents = 28,
            currentStudents = 28,
            schoolName = "싸피 초등학교",
        )
    )
    
    private val mockPrograms = mutableListOf<Program>(
        Program(
            id = "program_1",
            classId = "class_1",
            title = "과학 실험실 견학",
            description = "과학관에서 진행하는 실험 체험 프로그램",
            location = "국립과천과학관",
            date = "2025-08-15",
            startTime = "09:00",
            endTime = "15:00",
            maxParticipants = 30,
            participants = 25,
            status = ProgramStatus.UPCOMING
        ),
        Program(
            id = "program_2",
            classId = "class_1",
            title = "역사 박물관 탐방",
            description = "한국사 학습을 위한 박물관 견학",
            location = "국립중앙박물관",
            date = "2025-07-20",
            startTime = "10:00",
            endTime = "16:00",
            maxParticipants = 30,
            participants = 25,
            status = ProgramStatus.COMPLETED
        )
    )
    
    private val mockUsers = mutableListOf<User>(
        User(
            id = "user_1",
            userName = "student1",
            email = "student1@example.com",
            role = UserRole.STUDENT,
            studentId = "2025001",
            phoneNumber = "010-1234-5678"
        ),
        User(
            id = "user_2",
            userName = "student2", 
            email = "student2@example.com",
            role = UserRole.STUDENT,
            studentId = "2025002",
            phoneNumber = "010-2345-6789"
        )
    )
    
    private val mockTeams = mutableListOf<Team>(
        Team(
            id = "team_1",
            classId = "class_1",
            name = "탐험대",
            description = "호기심 가득한 탐험대입니다",
            leaderId = "user_1",
            memberIds = listOf("user_1", "user_2"),
            maxMembers = 4,
            currentMembers = 2,
            color = "#6200EE"
        )
    )
    
    private val mockMissions = mutableListOf<Mission>(
        Mission(
            id = "mission_1",
            programId = "program_1",
            title = "실험실 찾기",
            description = "물리 실험실을 찾아보세요",
            location = "물리 실험실",
            latitude = 37.4265,
            longitude = 126.9516,
            order = 1,
            type = MissionType.LOCATION,
            isRequired = true,
            points = 100
        )
    )
    
    private val mockTeamProgress = mutableListOf<TeamMissionProgress>()
    
    suspend fun getAllClasses(): List<Class> {
        delay(500) // 네트워크 지연 시뮬레이션
        return mockClasses.toList()
    }
    
    suspend fun getClassById(classId: String): Class? {
        delay(300)
        return mockClasses.find { it.id == classId }
    }
    
    suspend fun createClass(classData: Class): Class {
        delay(800)
        val newClass = classData.copy(
            id = "class_${System.currentTimeMillis()}",
        )
        mockClasses.add(newClass)
        return newClass
    }
    
    suspend fun getProgramsByClassId(classId: String): List<Program> {
        delay(400)
        return mockPrograms.filter { it.classId == classId }
    }
    
    suspend fun getMembersByClassId(classId: String): List<User> {
        delay(300)
        return mockUsers.toList()
    }
    
    suspend fun getTeamsByClassId(classId: String): List<Team> {
        delay(300)
        return mockTeams.filter { it.classId == classId }
    }
    
    suspend fun createTeam(team: Team): Team {
        delay(500)
        val newTeam = team.copy(
            id = "team_${System.currentTimeMillis()}"
        )
        mockTeams.add(newTeam)
        return newTeam
    }
    
    suspend fun updateTeam(team: Team): Team {
        delay(400)
        val index = mockTeams.indexOfFirst { it.id == team.id }
        if (index != -1) {
            mockTeams[index] = team
        }
        return team
    }
    
    suspend fun deleteTeam(teamId: String): Boolean {
        delay(300)
        return mockTeams.removeIf { it.id == teamId }
    }
    
    suspend fun getMissionsByProgramId(programId: String): List<Mission> {
        delay(300)
        return mockMissions.filter { it.programId == programId }
    }
    
    suspend fun getTeamProgressByProgramId(programId: String): List<TeamMissionProgress> {
        delay(300)
        return mockTeamProgress.filter { it.programId == programId }
    }
}