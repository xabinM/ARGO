package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.teamdelete.ClassTeamStatus;
import com.argo.backend.organization.dto.teamdelete.DeletedTeamInfo;
import com.argo.backend.organization.dto.teamdelete.TeamDeleteResponse;
import com.argo.backend.organization.dto.teamdelete.UnassignedStudentInfo;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.classroomlist.TeamRepository;
import com.argo.backend.organization.service.exception.AccessDeniedException;
import com.argo.backend.organization.service.exception.ClassNotFoundException;
import com.argo.backend.organization.service.exception.TeacherNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeamDeleteService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TeamRepository teamRepository;

    public TeamDeleteResponse deleteTeam(Long classId, Long teamId, Long userId) {
        // 권한 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (user.getRole() != Role.TEACHER) {
            throw new TeacherNotAllowedException("선생님만 팀을 삭제할 수 있습니다.");
        }

        // 반 조회 - applications와 teams 데이터 로드 (MultipleBagFetchException 방지)
        ClassRoom classRoom = classRoomRepository.findByIdWithApplications(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        
        // teams 데이터 별도 로드
        ClassRoom classRoomWithTeams = classRoomRepository.findByIdWithTeams(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        classRoom.setTeams(classRoomWithTeams.getTeams());

        if (!classRoom.getTeacher().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("해당 팀을 삭제할 권한이 없습니다.");
        }

        // 팀 조회
        Team team = teamRepository.findByIdAndClassRoom(teamId, classRoom)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        // 팀원들 미배정 처리
        List<User> teamMembers = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(ClassApplication::getUser)
                .filter(student -> student.getTeam() != null && 
                                 student.getTeam().getTeamId().equals(teamId))
                .collect(Collectors.toList());

        LocalDateTime deletedAt = LocalDateTime.now();
        
        for (User member : teamMembers) {
            member.setTeam(null);
            userRepository.save(member);
        }

        // 팀 삭제
        teamRepository.delete(team);

        log.info("팀 삭제 완료 - teamId: {}, unassignedStudents: {}", teamId, teamMembers.size());

        return createDeleteResponse(team, classRoom, teamMembers, deletedAt);
    }

    private TeamDeleteResponse createDeleteResponse(Team team, ClassRoom classRoom, 
                                                  List<User> unassignedStudents, 
                                                  LocalDateTime deletedAt) {
        // 삭제된 팀 정보
        DeletedTeamInfo deletedTeam = DeletedTeamInfo.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .deletedAt(deletedAt)
                .build();

        // 미배정된 학생 정보
        List<UnassignedStudentInfo> unassignedStudentInfos = unassignedStudents.stream()
                .map(student -> UnassignedStudentInfo.from(student, deletedAt))
                .collect(Collectors.toList());

        // 현재 반 팀 현황
        ClassTeamStatus classTeamStatus = calculateClassTeamStatus(classRoom);

        return TeamDeleteResponse.builder()
                .deletedTeam(deletedTeam)
                .unassignedStudents(unassignedStudentInfos)
                .classTeamStatus(classTeamStatus)
                .build();
    }

    private ClassTeamStatus calculateClassTeamStatus(ClassRoom classRoom) {
        List<User> allStudents = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(ClassApplication::getUser)
                .collect(Collectors.toList());

        int assignedStudents = (int) allStudents.stream()
                .filter(student -> student.getTeam() != null)
                .count();

        return ClassTeamStatus.builder()
                .totalTeams(classRoom.getTeams().size())
                .totalStudents(allStudents.size())
                .assignedStudents(assignedStudents)
                .unassignedStudents(allStudents.size() - assignedStudents)
                .build();
    }
}