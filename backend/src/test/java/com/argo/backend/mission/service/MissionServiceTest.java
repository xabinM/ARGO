package com.argo.backend.mission.service;

import com.argo.backend.domain.cardgame.repository.CardRepository;
import com.argo.backend.domain.cardgame.repository.TeamCardRepository;
import com.argo.backend.domain.mission.entity.MissionSession;
import com.argo.backend.domain.mission.enums.MissionSessionStatus;
import com.argo.backend.domain.mission.repository.MissionSessionRepository;
import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.ploblem.entity.QuizProblem;
import com.argo.backend.domain.ploblem.repository.SelfieProblemRepository;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.spot.repository.SpotRepository;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.mission.dto.missionCreate.MissionCreateDto;
import com.argo.backend.mission.exception.InvalidMissionSessionException;
import com.argo.backend.mission.exception.MissionAlreadyCompletedException;
import com.argo.backend.mission.exception.ProblemNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock private MissionSessionRepository missionSessionRepository;
    @Mock private SpotRepository spotRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private CardRepository cardRepository;
    @Mock private TeamCardRepository teamCardRepository;
    @Mock private SelfieProblemRepository selfieProblemRepository;

    @InjectMocks
    private MissionService missionService;

    // ===== createMission 테스트 =====

    @Test
    void createMission_미션세션없고_문제존재시_성공() {
        Long teamId = 1L;
        Long spotId = 2L;

        Spot spot = mock(Spot.class);
        Problem problem = mock(QuizProblem.class);
        spot.setProblems(List.of(problem));

        Team team = mock(Team.class);

        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(missionSessionRepository.findByTeamAndSpot(team, spot)).thenReturn(Optional.empty());
        when(selfieProblemRepository.findAll()).thenReturn(Collections.emptyList());
        when(missionSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MissionCreateDto dto = missionService.createMission(teamId, spotId);

        assertThat(dto).isNotNull();
        assertThat(dto.getProblemDetail()).isNotNull();
    }

    @Test
    void createMission_문제없으면_예외발생() {
        Long teamId = 1L;
        Long spotId = 2L;

        Spot spot = mock(Spot.class);
        spot.setProblems(Collections.emptyList());
        Team team = mock(Team.class);

        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(missionSessionRepository.findByTeamAndSpot(team, spot)).thenReturn(Optional.empty());
        when(selfieProblemRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> missionService.createMission(teamId, spotId))
                .isInstanceOf(ProblemNotFoundException.class);
    }

    @Test
    void createMission_이미완료된미션이면_예외발생() {
        Long teamId = 1L;
        Long spotId = 2L;

        Spot spot = mock(Spot.class);
        Team team = mock(Team.class);

        MissionSession completedSession = mock(MissionSession.class);
        when(completedSession.getStatus()).thenReturn(MissionSessionStatus.COMPLETED);

        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(missionSessionRepository.findByTeamAndSpot(team, spot)).thenReturn(Optional.of(completedSession));

        assertThatThrownBy(() -> missionService.createMission(teamId, spotId))
                .isInstanceOf(MissionAlreadyCompletedException.class);
    }

}