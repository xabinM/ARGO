package com.argo.backend.mission.service;

import com.argo.backend.domain.cardgame.entity.Card;
import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.cardgame.enums.CardTier;
import com.argo.backend.domain.cardgame.repository.CardRepository;
import com.argo.backend.domain.cardgame.repository.TeamCardRepository;
import com.argo.backend.domain.mission.entity.MissionSession;
import com.argo.backend.domain.mission.enums.MissionSessionStatus;
import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.mission.dto.SubmitMission.MissionSubmitDto;
import com.argo.backend.mission.dto.common.ProblemDetail;
import com.argo.backend.mission.dto.missionCreate.MissionCreateDto;
import com.argo.backend.mission.exception.*;
import com.argo.backend.domain.mission.repository.MissionSessionRepository;
import com.argo.backend.domain.spot.repository.SpotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionSessionRepository missionSessionRepository;
    private final SpotRepository spotRepository;
    private final TeamRepository teamRepository;
    private final CardRepository cardRepository;
    private final TeamCardRepository teamCardRepository;

    @Transactional
    public MissionCreateDto createMission(Long teamId, Long spotId) {
        Spot spot = getSpotById(spotId);
        Team team = getTeamById(teamId);

        MissionSession missionSession = getOrCreateMissionSession(team, spot);
        ProblemDetail problemDetail = ProblemDetail.from(missionSession.getProblem());

        return new MissionCreateDto(missionSession.getSessionId(), problemDetail);
    }

    private Spot getSpotById(Long spotId) {
        return spotRepository.findById(spotId)
                .orElseThrow(SpotNotFoundException::new);
    }

    private Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    private MissionSession getOrCreateMissionSession(Team team, Spot spot) {
        return missionSessionRepository.findByTeamAndSpot(team, spot)
                .map(session -> {
                    if (session.getStatus() == MissionSessionStatus.COMPLETED) {
                        throw new MissionAlreadyCompletedException();
                    }
                    return session;
                })
                .orElseGet(() -> createNewMissionSession(team, spot));
    }

    private MissionSession createNewMissionSession(Team team, Spot spot) {
        List<Problem> problems = spot.getProblems();
        Problem problem = findRandomProblem(problems);

        if (problem == null) {
            throw new ProblemNotFoundException();
        }

        MissionSession session = MissionSession.from(spot, team, problem);
        missionSessionRepository.save(session);
        return session;
    }

    private Problem findRandomProblem(List<Problem> problems) {
        if (problems.isEmpty()) return null;

        Collections.shuffle(problems);
        return problems.get(0);
    }


    @Transactional
    public MissionSubmitDto submitMission(Long missionId, boolean isSuccessful) {
        MissionSession missionSession = getMissionSession(missionId);

        if (!missionSession.isStatusStarted()) {
            throw new InvalidMissionSessionException();
        }

        if (isSuccessful) {
            return handleSuccessfulMission(missionSession);
        } else {
            return handleFailedMission(missionSession);
        }
    }

    private MissionSession getMissionSession(Long missionId) {
        return missionSessionRepository.findById(missionId)
                .orElseThrow(MissionSessionNotFoundException::new);
    }

    private MissionSubmitDto handleSuccessfulMission(MissionSession missionSession) {
        missionSession.alterSuccessfulTrue();

        Card card = pickRandomCard(missionSession.getSpot().getId());
        CardTier tier = CardTier.getRandomByCardTier(card);

        TeamCard teamCard = TeamCard.from(missionSession.getTeam(), card, tier);
        teamCardRepository.save(teamCard);

        missionSession.alterMissionStatus();

        return MissionSubmitDto.success(card.getCardId(), tier);
    }

    private MissionSubmitDto handleFailedMission(MissionSession missionSession) {
        missionSession.alterSuccessfulFalse();
        missionSession.alterMissionStatus();

        return MissionSubmitDto.fail();
    }

    private Card pickRandomCard(Long spotId) {
        List<Card> cards = cardRepository.findAllBySpotId(spotId);

        if (cards.isEmpty()) {
            cards = cardRepository.findAllBySpotIsNull();
        }

        if (cards.isEmpty()) {
            throw new CardNotExistException();
        }

        Collections.shuffle(cards);
        return cards.get(0);
    }
}
