package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.response.ClassDataDto
import com.example.bogoargo.data.model.Class
import com.example.bogoargo.data.response.ClassLeaveDataDto
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun ClassDataDto.toDomainModel(): Class {
    return Class(
        classId = this.classId,
        className = this.className,
        description = this.description,
        location = this.location,
        activityDate = try {
            LocalDate.parse(this.activityDate.substringBefore("T")) // "YYYY-MM-DDTHH:MM:SS" -> "YYYY-MM-DD"
        } catch (e: DateTimeParseException) {
            LocalDate.MIN
        },
        currentStudents = this.studentCount,
        maxStudents = this.maxStudents,
        studentCount = this.students.size,
        teamCount = this.teamCount,
        status = when (this.status) {
            "ACTIVE" -> Class.ClassStatus.ACTIVE
            "ENDED" -> Class.ClassStatus.ENDED
            else -> Class.ClassStatus.ACTIVE
        },
        inviteCode = this.inviteCode,
        createdAt = try {
            LocalDate.parse(this.createdAt.substringBefore("T"))
        } catch (e: DateTimeParseException) {
            LocalDate.MIN
        }
    )
}

// TODO: 반 떠나기 후 결과 출력
// ClassLeaveDataDto를 ClassLeaveInfo Model로 변환하는 확장 함수
//fun ClassLeaveDataDto.toDomainModel(): ClassLeaveDataDto {
//    return ClassLeaveDataDto(
//        leftClass = this.leftClass.toDomainModel(), // ClassDataDto -> Class 변환
//        teamInfo = this.teamInfo.toTeamDomainModel(), // TeamDataDto -> Team 변환
//        studentInfo = this.studentInfo.toUserDomainModel() // UserDataDto -> User 변환
//    )
//}