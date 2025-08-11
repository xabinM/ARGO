package com.argo.backend.cardgame.util;

import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.entity.UserTeam;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.domain.user.repository.UserTeamRepository;

import java.util.Optional;
import com.argo.backend.organization.exception.types.TeamNotFoundException;
import com.argo.backend.organization.exception.types.UnauthorizedClassAccessException;
import com.argo.backend.organization.exception.types.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamAccessValidator {
    
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    
    /**
     * 사용자 검증 및 조회
     */
    public User validateAndGetUser(Long userId) {
        if (userId == null) {
            throw new UserNotFoundException();
        }
        
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }
    
    /**
     * 팀 접근 권한 검증 (UserTeam 기반) - user, teamId
     */
    public Team validateTeamAccess(Long teamId, User user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);
                
        // N+1 문제 해결: Repository 메서드로 대체 (FETCH JOIN 사용)
        Optional<UserTeam> userTeamOpt = userTeamRepository.findActiveByUserIdAndClassId(user.getUserId(), team.getClassRoom().getClassId());
        Team userTeam = userTeamOpt.map(UserTeam::getTeam).orElse(null);
        
        if (userTeam == null || !userTeam.getTeamId().equals(teamId)) {
            throw new UnauthorizedClassAccessException();
        }
        
        return team;
    }
    
    /**
     * 팀 접근 권한 검증 (teamId + userId)
     */
    public Team validateTeamAccess(Long teamId, Long userId) {
        User user = validateAndGetUser(userId);
        return validateTeamAccess(teamId, user);
    }
    
    /**
     * 사용자의 특정 클래스 팀 조회 및 권한 검증 (성능 최적화)
     */
    public Team validateAndGetUserTeamInClass(Long userId, Long classId) {
        User user = validateAndGetUser(userId);
        
        // Repository를 통한 직접 조회로 성능 최적화
        return userTeamRepository.findActiveByUserIdAndClassId(userId, classId)
                .map(ut -> ut.getTeam())
                .orElse(null);
    }
    
    /**
     * 사용자가 특정 팀의 멤버인지 확인
     */
    public boolean isUserMemberOfTeam(Long userId, Long teamId) {
        return userTeamRepository.existsActiveByUserIdAndTeamId(userId, teamId);
    }
}