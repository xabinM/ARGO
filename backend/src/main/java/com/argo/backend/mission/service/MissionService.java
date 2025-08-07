package com.argo.backend.mission.service;

import com.argo.backend.domain.mission.entity.MissionSession;
import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.mission.dto.common.ProblemResponseDto;
import com.argo.backend.mission.exception.ProblemNotFoundException;
import com.argo.backend.mission.exception.SpotNotFoundException;
import com.argo.backend.mission.exception.TeamNotFoundException;
import com.argo.backend.domain.mission.repository.MissionSessionRepository;
import com.argo.backend.domain.ploblem.repository.ProblemRepository;
import com.argo.backend.domain.spot.repository.SpotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionSessionRepository missionSessionRepository;
    private final SpotRepository spotRepository;
    private final TeamRepository teamRepository;
    private final ProblemRepository problemRepository;

    @Transactional
    public ProblemResponseDto createMission(Long teamId, Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(SpotNotFoundException::new);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        List<Problem> problems = problemRepository.findRandomProblems();

        Problem problem = findRandomProblem(problems);
        if (problem == null) {
            throw new ProblemNotFoundException();
        }

        MissionSession missionSession = MissionSession.from(spot, team, problem);
        missionSessionRepository.save(missionSession);

        return ProblemResponseDto.from(missionSession.getProblem());
    }

    private Problem findRandomProblem(List<Problem> problems) {
        if (problems.isEmpty()) {
            return null;
        }
        return problems.get(0);
    }
}
