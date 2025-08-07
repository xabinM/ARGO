package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.response.ClassDataDto
import com.example.bogoargo.domain.model.Class
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun ClassDataDto.toDomainModel(): Class {
    return Class(
        classId = this.classId,
        className = this.className,
        description = this.description ?: "",
        location = this.location ?: "",
        activityDate = try {
            this.activityDate?.substringBefore("T")?.let { LocalDate.parse(it) } ?: LocalDate.MIN
        } catch (e: DateTimeParseException) {
            LocalDate.MIN
        },
        maxStudents = this.maxStudents ?: 0,
        studentCount = this.students?.size ?: 0,
        teamCount = this.teams?.size ?:0,
        status = when (this.status?.uppercase()) {
            "ACTIVE" -> Class.ClassStatus.ACTIVE
            "ENDED" -> Class.ClassStatus.ENDED
            else -> Class.ClassStatus.ACTIVE
        },
        inviteCode = this.inviteCode,
        createdAt = try {
            this.createdAt.substringBefore("T")?.let { LocalDate.parse(it) } ?: LocalDate.MIN
        } catch (e: DateTimeParseException) {
            LocalDate.MIN
        },
        isFull = (this.students?.size ?: 0) >= (this.maxStudents ?: 0) // maxStudents도 nullable
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