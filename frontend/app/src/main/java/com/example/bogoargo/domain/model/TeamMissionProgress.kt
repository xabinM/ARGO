package com.example.bogoargo.domain.model

data class TeamMissionProgress(
    val id: String = "",
    val teamId: String = "",
    val programId: String = "",
    val missionId: String = "",
    val status: MissionProgressStatus = MissionProgressStatus.NOT_STARTED,
    val completedAt: String? = null,
    val score: Int = 0,
    val timeSpent: Int = 0, // 소요 시간 (분)
    val submissionData: String = "", // 제출 데이터 (사진 URL, 답안 등)
    val feedback: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

enum class MissionProgressStatus {
    NOT_STARTED,    // 미시작
    IN_PROGRESS,    // 진행중
    COMPLETED,      // 완료
    SKIPPED         // 건너뜀
}

data class TeamProgramProgress(
    val id: String = "",
    val teamId: String = "",
    val programId: String = "",
    val totalMissions: Int = 0,
    val completedMissions: Int = 0,
    val totalScore: Int = 0,
    val maxScore: Int = 0,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: ProgramProgressStatus = ProgramProgressStatus.NOT_STARTED
)

enum class ProgramProgressStatus {
    NOT_STARTED,    // 미시작
    IN_PROGRESS,    // 진행중
    COMPLETED,      // 완료
    ABANDONED       // 중단
}