package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.response.ClassDataDto
import com.example.bogoargo.data.response.ClassInfoDto
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
        currentStudents = this.students?.size ?: 0,
        maxStudents = this.maxStudents ?: 0,
        studentCount = this.students?.size ?: 0,
        teamCount = this.teams?.size ?:0,
        grade = this.grade ?: 1,
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

// ClassInfoDto를 Class 도메인 모델로 변환하는 확장 함수
fun ClassInfoDto.toDomainModel(): Class {
    return Class(
        classId = this.classId,
        className = this.className,
        description = this.description,
        location = this.location,
        activityDate = try {
            LocalDate.parse(this.activityDate.substringBefore("T"))
        } catch (e: DateTimeParseException) {
            LocalDate.MIN
        },
        currentStudents = this.studentCount,
        maxStudents = this.maxStudents,
        studentCount = this.studentCount,
        teamCount = this.teamCount,
        grade = 1, // ClassInfoDto에 grade 필드가 없으므로 기본값 사용
        status = when (this.status.uppercase()) {
            "ACTIVE" -> Class.ClassStatus.ACTIVE
            "ENDED" -> Class.ClassStatus.ENDED
            else -> Class.ClassStatus.ACTIVE
        },
        inviteCode = this.inviteCode ?: "",
        createdAt = try {
            LocalDate.parse(this.createdAt.substringBefore("T"))
        } catch (e: DateTimeParseException) {
            LocalDate.MIN
        },
        isFull = this.studentCount >= this.maxStudents
    )
}

