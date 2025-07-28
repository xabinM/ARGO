package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.teamcreate.TeamCreateRequest;
import com.argo.backend.organization.dto.teamcreate.TeamCreateResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeamCreateService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TeamRepository teamRepository;

    public TeamCreateResponse createTeam(Long classId, Long userId, TeamCreateRequest request) {
        // 1. 입력값 유효성 검증
        validateRequest(request);

        // 2. 사용자 조회 및 권한 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        validateTeacherRole(user);

        // 3. 반 조회 (N+1 문제 방지)
        ClassRoom classRoom = classRoomRepository.findByIdWithBasicDetails(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));

        // 4. 권한 검증 - 본인이 생성한 반인지 확인
        validateClassOwnership(classRoom, user);

        // 5. 팀 이름 중복 검증
        validateDuplicateTeamName(classRoom, request.getTeamNameTrimmed());

        // 6. 팀 생성
        Team team = createTeamEntity(classRoom, request);
        Team savedTeam = teamRepository.save(team);

        log.info("팀 생성 완료 - userId: {}, classId: {}, teamId: {}, teamName: {}", 
                userId, classId, savedTeam.getTeamId(), savedTeam.getTeamName());

        return TeamCreateResponse.from(savedTeam, classRoom);
    }

    private void validateRequest(TeamCreateRequest request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("팀 이름을 입력해주세요.");
        }
    }

    private void validateTeacherRole(User user) {
        if (user.getRole() != Role.TEACHER) {
            throw new TeacherNotAllowedException("선생님만 팀을 생성할 수 있습니다.");
        }
    }

    private void validateClassOwnership(ClassRoom classRoom, User user) {
        if (!classRoom.getTeacher().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("해당 반에서 팀을 생성할 권한이 없습니다.");
        }
    }

    private void validateDuplicateTeamName(ClassRoom classRoom, String teamName) {
        if (teamRepository.existsByClassRoomAndTeamName(classRoom, teamName)) {
            throw new IllegalArgumentException("이미 동일한 이름의 팀이 존재합니다.");
        }
    }

    private Team createTeamEntity(ClassRoom classRoom, TeamCreateRequest request) {
        return Team.builder()
                .teamName(request.getTeamNameTrimmed())
                .maxMembers(request.getMaxMembersWithDefault())
                .classRoom(classRoom)
                .build();
    }
}