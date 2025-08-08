package com.argo.backend.mission.service;

import com.argo.backend.domain.cardgame.entity.Card;
import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.cardgame.enums.CardTier;
import com.argo.backend.domain.cardgame.repository.CardRepository;
import com.argo.backend.domain.cardgame.repository.TeamCardRepository;
import com.argo.backend.domain.mission.entity.MissionSession;
import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.mission.dto.SubmitMission.MissionSubmitDto;
import com.argo.backend.mission.dto.common.ProblemDetail;
import com.argo.backend.mission.dto.missionCreate.MissionCreateDto;
import com.argo.backend.mission.exception.*;
import com.argo.backend.domain.mission.repository.MissionSessionRepository;
import com.argo.backend.domain.ploblem.repository.ProblemRepository;
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
    private final ProblemRepository problemRepository;
    private final CardRepository cardRepository;
    private final TeamCardRepository teamCardRepository;

    @Transactional
    public MissionCreateDto createMission(Long teamId, Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(SpotNotFoundException::new);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        List<Problem> problems = spot.getProblems();

        Problem problem = findRandomProblem(problems);
        if (problem == null) {
            throw new ProblemNotFoundException();
        }

        MissionSession missionSession = MissionSession.from(spot, team, problem);
        missionSessionRepository.save(missionSession);

        ProblemDetail problemDetail = ProblemDetail.from(missionSession.getProblem());

        return new MissionCreateDto(missionSession.getSessionId(), problemDetail);
    }

    private Problem findRandomProblem(List<Problem> problems) {
        if (problems.isEmpty()) {
            return null;
        }
        Collections.shuffle(problems);
        return problems.get(0);
    }

    @Transactional
    public MissionSubmitDto submitQuiz(Long missionId, boolean isSuccessful) {
        MissionSession missionSession = missionSessionRepository.findById(missionId)
                .orElseThrow(MissionSessionNotFoundException::new);

        if (!missionSession.isStatusStarted()) {
            throw new InvalidMissionSessionException();
        }

        System.out.println("-----------------isSuccessful : " + isSuccessful);

        if (isSuccessful) {
            System.out.println("성공 옴");
            missionSession.alterSuccessfulTrue();

            Card card = pickRandomCard(missionSession.getSpot().getId());
            CardTier tier = CardTier.getRandomByCardTier(card);

            TeamCard teamCard = TeamCard.from(
                    missionSession.getTeam(),
                    card,
                    tier
            );
            teamCardRepository.save(teamCard);

            missionSession.alterMissionStatus();

            return MissionSubmitDto.success(card.getCardId(), tier);
        } else {
            System.out.println("실패 옴");
            missionSession.alterSuccessfulFalse();
            missionSession.alterMissionStatus();

            return MissionSubmitDto.fail();
        }
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
