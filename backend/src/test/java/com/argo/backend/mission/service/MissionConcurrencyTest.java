package com.argo.backend.mission.service;

import com.argo.backend.domain.cardgame.entity.Card;
import com.argo.backend.domain.cardgame.repository.CardRepository;
import com.argo.backend.domain.cardgame.repository.TeamCardRepository;
import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.classroom.repository.ClassApplicationRepository;
import com.argo.backend.domain.classroom.repository.ClassRoomRepository;
import com.argo.backend.domain.common.Coordinates;
import com.argo.backend.domain.location.entity.Location;
import com.argo.backend.domain.location.repository.LocationRepository;
import com.argo.backend.domain.mission.entity.MissionSession;
import com.argo.backend.domain.mission.repository.MissionSessionRepository;
import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.ploblem.entity.QuizProblem;
import com.argo.backend.domain.ploblem.repository.ProblemRepository;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.spot.repository.SpotRepository;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.user.entity.Teacher;
import com.argo.backend.domain.user.repository.TeacherRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MissionConcurrencyTest {

    @Autowired
    private MissionService missionService;

    @Autowired
    private MissionSessionRepository missionSessionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ClassRoomRepository classRoomRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TeamCardRepository teamCardRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ClassApplicationRepository classApplicationRepository;

    @AfterEach
    void tearDown() {
        // 자식 테이블부터 순서대로 삭제
        teamCardRepository.deleteAll();
        missionSessionRepository.deleteAll();
        teamRepository.deleteAll();
        cardRepository.deleteAll();
        problemRepository.deleteAll();
        spotRepository.deleteAll();
        classApplicationRepository.deleteAll(); // ClassApplication 삭제 추가
        classRoomRepository.deleteAll();
        teacherRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 100명이 미션 완료 요청을 보내도 단 1번만 성공해야 한다")
    void submitMissionConcurrencyTest() throws InterruptedException {
        // given
        Teacher teacher = teacherRepository.save(Teacher.from("teacher", "password", "Teacher Name"));
        
        Coordinates coordinates = Coordinates.create(BigDecimal.valueOf(37.0), BigDecimal.valueOf(127.0));
        Location location = locationRepository.save(Location.create("Test Location", coordinates));

        ClassRoom classRoom = classRoomRepository.save(ClassRoom.from(
                teacher,
                "Test Class",
                "Description",
                LocalDate.now(),
                "TEST_CODE",
                30,
                location,
                1
        ));

        Team team = teamRepository.save(Team.from(classRoom, "Test Team", 4));

        Spot spot = spotRepository.save(Spot.create(
                "Test Spot",
                "Description",
                coordinates,
                location
        ));

        Problem problem = problemRepository.save(QuizProblem.from(
                spot,
                1,
                "Question",
                Collections.singletonList("Choice"),
                0,
                "Explanation"
        ));

        // 카드 데이터 준비 (보상 지급용)
        cardRepository.save(Card.from(
                location,
                spot,
                "Test Card",
                "Description",
                10,
                10,
                false
        ));

        MissionSession missionSession = MissionSession.from(spot, team, problem);
        missionSessionRepository.save(missionSession);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    missionService.submitMission(missionSession.getSessionId(), true);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 끝날 때까지 대기

        // then
        Team updatedTeam = teamRepository.findById(team.getTeamId()).orElseThrow();
        long teamCardCount = teamCardRepository.count();

        assertThat(successCount.get()).isEqualTo(1); // 성공은 딱 1번
        assertThat(failCount.get()).isEqualTo(threadCount - 1); // 나머지는 모두 실패
        assertThat(updatedTeam.getGameResult().getWins()).isEqualTo(1); // 승리 횟수 1회 증가
        assertThat(updatedTeam.getGameResult().getTotalPoints()).isEqualTo(50); // 점수 50점 증가
        assertThat(teamCardCount).isEqualTo(1); // 카드 1장만 지급됨
    }
}
