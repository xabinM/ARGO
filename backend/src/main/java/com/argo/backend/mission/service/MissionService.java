package com.argo.backend.mission.service;

import com.argo.backend.domain.mission.MissionSession;
import com.argo.backend.domain.ploblem.Problem;
import com.argo.backend.domain.spot.Spot;
import com.argo.backend.domain.team.Team;
import com.argo.backend.mission.dto.missionCreate.MissionCreateDto;
import com.argo.backend.mission.exception.ProblemNotFoundException;
import com.argo.backend.mission.exception.SpotNotFoundException;
import com.argo.backend.mission.exception.TeamNotFoundException;
import com.argo.backend.mission.repository.MissionSessionRepository;
import com.argo.backend.mission.repository.ProblemRepository;
import com.argo.backend.mission.repository.SpotRepository;
import com.argo.backend.mission.repository.TeamRepository;
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
    public MissionCreateDto createMission(Long teamId, Long spotId) {
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

        return new MissionCreateDto(missionSession.getProblem());
    }

    private Problem findRandomProblem(List<Problem> problems) {
        if (problems.isEmpty()) {
            return null;
        }
        return problems.get(0);
    }
}
