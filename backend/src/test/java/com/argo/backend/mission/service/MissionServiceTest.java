package com.argo.backend.mission.service;

import com.argo.backend.domain.cardgame.entity.Card;
import com.argo.backend.domain.cardgame.entity.TeamCard;
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
import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.mission.dto.SubmitMission.MissionSubmitDto;
import com.argo.backend.mission.dto.missionCreate.MissionCreateDto;
import com.argo.backend.mission.dto.missionPossibleCheck.MissionPossibleCheckDto;
import com.argo.backend.mission.exception.CardNotExistException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private MissionSessionRepository missionSessionRepository;
    @Mock
    private SpotRepository spotRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private TeamCardRepository teamCardRepository;
    @Mock
    private SelfieProblemRepository selfieProblemRepository;

    @InjectMocks
    private MissionService missionService;

    // ===== createMission 테스트 =====

    @Test
    void createMission_미션세션없고_문제존재시_성공() {
        final Long teamId = 1L;
        final Long spotId = 2L;

        final Spot spot = new Spot();
        final Problem problem = mock(QuizProblem.class);
        spot.setProblems(List.of(problem));

        final Team team = mock(Team.class);

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
        final Long teamId = 1L;
        final Long spotId = 2L;

        final Spot spot = mock(Spot.class);
        spot.setProblems(Collections.emptyList());
        final Team team = mock(Team.class);

        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(missionSessionRepository.findByTeamAndSpot(team, spot)).thenReturn(Optional.empty());
        when(selfieProblemRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> missionService.createMission(teamId, spotId))
                .isInstanceOf(ProblemNotFoundException.class);
    }

    @Test
    void createMission_이미완료된미션이면_예외발생() {
        final Long teamId = 1L;
        final Long spotId = 2L;

        final Spot spot = mock(Spot.class);
        final Team team = mock(Team.class);

        final MissionSession completedSession = mock(MissionSession.class);
        when(completedSession.getStatus()).thenReturn(MissionSessionStatus.COMPLETED);

        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(missionSessionRepository.findByTeamAndSpot(team, spot)).thenReturn(Optional.of(completedSession));

        assertThatThrownBy(() -> missionService.createMission(teamId, spotId))
                .isInstanceOf(MissionAlreadyCompletedException.class);
    }

    // ===== submitMission 테스트 =====

    @Test
    void submitMission_시작안된미션이면_예외발생() {
        final Long missionId = 1L;
        final MissionSession missionSession = mock(MissionSession.class);

        when(missionSessionRepository.findById(missionId)).thenReturn(Optional.of(missionSession));
        when(missionSession.isStatusStarted()).thenReturn(false);

        assertThatThrownBy(() -> missionService.submitMission(missionId, true))
                .isInstanceOf(InvalidMissionSessionException.class);
    }

    @Test
    void submitMission_성공케이스() {
        final Long missionId = 1L;
        final MissionSession missionSession = mock(MissionSession.class);
        final Spot spot = mock(Spot.class);
        spot.setId(10L);
        final Card card = mock(Card.class);

        when(missionSessionRepository.findById(missionId)).thenReturn(Optional.of(missionSession));
        when(missionSession.isStatusStarted()).thenReturn(true);
        when(missionSession.getSpot()).thenReturn(spot);
        when(cardRepository.findAllBySpotId(spot.getId())).thenReturn(List.of(card));

        final MissionSubmitDto dto = missionService.submitMission(missionId, true);

        assertThat(dto.successful()).isTrue();
        verify(teamCardRepository, times(1)).save(any(TeamCard.class));
    }

    @Test
    void submitMission_카드없으면_nullSpot카드조회() {
        final Long missionId = 1L;
        final MissionSession missionSession = mock(MissionSession.class);
        final Spot spot = mock(Spot.class);
        spot.setId(10L);
        final Card card = mock(Card.class);

        when(missionSessionRepository.findById(missionId)).thenReturn(Optional.of(missionSession));
        when(missionSession.isStatusStarted()).thenReturn(true);
        when(missionSession.getSpot()).thenReturn(spot);
        when(cardRepository.findAllBySpotId(spot.getId())).thenReturn(Collections.emptyList());
        when(cardRepository.findAllBySpotIsNull()).thenReturn(List.of(card));

        final MissionSubmitDto dto = missionService.submitMission(missionId, true);

        assertThat(dto.successful()).isTrue();
    }

    @Test
    void submitMission_카드전혀없으면_예외() {
        final Long missionId = 1L;
        final MissionSession missionSession = mock(MissionSession.class);
        final Spot spot = mock(Spot.class);
        spot.setId(10L);

        when(missionSessionRepository.findById(missionId)).thenReturn(Optional.of(missionSession));
        when(missionSession.isStatusStarted()).thenReturn(true);
        when(missionSession.getSpot()).thenReturn(spot);
        when(cardRepository.findAllBySpotId(spot.getId())).thenReturn(Collections.emptyList());
        when(cardRepository.findAllBySpotIsNull()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> missionService.submitMission(missionId, true))
                .isInstanceOf(CardNotExistException.class);
    }

    @Test
    void submitMission_실패케이스() {
        final Long missionId = 1L;
        final MissionSession missionSession = mock(MissionSession.class);

        when(missionSessionRepository.findById(missionId)).thenReturn(Optional.of(missionSession));
        when(missionSession.isStatusStarted()).thenReturn(true);

        final MissionSubmitDto dto = missionService.submitMission(missionId, false);

        assertThat(dto.successful()).isFalse();
    }

    // ===== checkPossibleMissionSpot 테스트 =====

    @Test
    void checkPossibleMissionSpot_미션없으면_true반환() {
        final Long teamId = 1L;
        final Long spotId = 2L;
        final Spot spot = mock(Spot.class);
        final Team team = mock(Team.class);

        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(missionSessionRepository.findByTeamAndSpot(team, spot)).thenReturn(Optional.empty());

        final MissionPossibleCheckDto dto = missionService.checkPossibleMissionSpot(teamId, spotId);

        assertThat(dto.isSuccess()).isTrue();
        assertThat(dto.getMessage()).isNull();
    }

    @Test
    void checkPossibleMissionSpot_미션있으면_false반환() {
        final Long teamId = 1L;
        final Long spotId = 2L;
        final Spot spot = mock(Spot.class);
        final Team team = mock(Team.class);
        final MissionSession session = mock(MissionSession.class);

        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(missionSessionRepository.findByTeamAndSpot(team, spot)).thenReturn(Optional.of(session));

        final MissionPossibleCheckDto dto = missionService.checkPossibleMissionSpot(teamId, spotId);

        assertThat(dto.isSuccess()).isFalse();
        assertThat(dto.getMessage()).isEqualTo(ResponseMessage.ALREADY_PROGRESSED_MISSION.getMessage());
    }
}