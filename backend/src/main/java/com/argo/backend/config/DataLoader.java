package com.argo.backend.config;

import com.argo.backend.domain.cardgame.entity.Card;
import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.cardgame.enums.CardTier;
import com.argo.backend.domain.cardgame.repository.CardRepository;
import com.argo.backend.domain.cardgame.repository.TeamCardRepository;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.spot.repository.SpotRepository;
import com.argo.backend.domain.ploblem.entity.QuizProblem;
import com.argo.backend.domain.ploblem.repository.QuizProblemRepository;
import com.argo.backend.domain.user.entity.Teacher;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.entity.UserTeam;
import com.argo.backend.domain.user.enums.Role;
import com.argo.backend.domain.user.repository.TeacherRepository;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.domain.user.repository.UserTeamRepository;
import com.argo.backend.domain.location.entity.Location;
import com.argo.backend.domain.location.repository.LocationRepository;
import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.classroom.entity.ClassApplication;
import com.argo.backend.domain.classroom.enums.ApplicationStatus;
import com.argo.backend.domain.classroom.repository.ClassRoomRepository;
import com.argo.backend.domain.classroom.repository.ClassApplicationRepository;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.common.Coordinates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final LocationRepository locationRepository;
    private final ClassRoomRepository classRoomRepository;
    private final ClassApplicationRepository classApplicationRepository;
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final SpotRepository spotRepository;
    private final CardRepository cardRepository;
    private final TeamCardRepository teamCardRepository;
    private final QuizProblemRepository quizProblemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 이미 데이터가 있으면 생성하지 않음
        if (userRepository.count() > 10) {
            log.info("데이터가 이미 존재하므로 초기 데이터 생성을 건너뜁니다.");
            return;
        }

        log.info("=== 초기 테스트 데이터 생성 시작 ===");

        // 1. SSAFY 위치 생성
        Location ssafyLocation = createLocation();

        // 2. 선생님 계정 생성 (teacher1, password1)
        Teacher teacher = createTeacher();

        // 3. 학생 계정들 생성 (60명)
        List<User> students = createStudents();

        // 4. 싸피3반 생성
        ClassRoom classRoom = createClassRoom(teacher, ssafyLocation);

        // 5. 모든 학생들을 반에 신청 및 승인
        applyStudentsToClass(students, classRoom);

        // 6. 10개 팀 생성
        List<Team> teams = createTeams(classRoom);

        // 7. 학생들을 팀에 배정 (각 팀 6명씩)
        assignStudentsToTeams(students, teams);

        // 8. 스팟 5개 생성 (SSAFY 1개 + 추가 4개)
        List<Spot> spots = createAllSpots(ssafyLocation);

        // 9. 5개 카드 생성 (1개는 스팟 관련, 4개는 일반)
        List<Card> cards = createCards(ssafyLocation, spots.get(0)); // 첫 번째 스팟(SSAFY)과 연결

        // 10. 각 스팟에 QuizProblem 생성 (총 5개)
        List<QuizProblem> problems = createAllQuizProblems(spots);

        log.info("=== 초기 테스트 데이터 생성 완료! ===");
        log.info("선생님: teacher1 / password1");
        log.info("학생들: student11~16, student21~26, ..., student101~106 / password1");
        log.info("반: 싸피3반 (SSAFY 위치)");
        log.info("팀: 10개 팀, 각 6명씩 배정");
        log.info("스팟: 5개 (SSAFY + 4개 추가 스팟)");
        log.info("퀴즈: 5개 (각 스팟별 1개씩)");
    }

    private List<Spot> createAllSpots(Location location) {
        List<Spot> spots = new ArrayList<>();

        // 1. SSAFY 스팟 (기존)
        Coordinates ssafyCoords = Coordinates.create(
                new BigDecimal("37.5012743"),
                new BigDecimal("127.0396220")
        );
        Spot ssafySpot = Spot.create("SSAFY 스팟", "SSAFY 교육장 메인 스팟", ssafyCoords, location);
        spots.add(spotRepository.save(ssafySpot));

        // 2. 학생 수 스팟
        Coordinates studentCountCoords = Coordinates.create(
                new BigDecimal("37.501347"),
                new BigDecimal("127.039765")
        );
        Spot studentCountSpot = Spot.create("학생 수 체크 스팟", "우리 반 학생 수를 확인하는 곳", studentCountCoords, location);
        spots.add(spotRepository.save(studentCountSpot));

        // 3. 도서관 위치 스팟
        Coordinates libraryCoords = Coordinates.create(
                new BigDecimal("37.501280"),
                new BigDecimal("127.039790")
        );
        Spot librarySpot = Spot.create("도서관 위치 스팟", "우리 반 도서관 위치를 확인하는 곳", libraryCoords, location);
        spots.add(spotRepository.save(librarySpot));

        // 4. 입실 시간 스팟
        Coordinates checkInCoords = Coordinates.create(
                new BigDecimal("37.501227"),
                new BigDecimal("127.039658")
        );
        Spot checkInSpot = Spot.create("입실 시간 스팟", "입실 마감 시간을 확인하는 곳", checkInCoords, location);
        spots.add(spotRepository.save(checkInSpot));

        // 5. 퇴실 시간 스팟
        Coordinates checkOutCoords = Coordinates.create(
                new BigDecimal("37.501249"),
                new BigDecimal("127.039600")
        );
        Spot checkOutSpot = Spot.create("퇴실 시간 스팟", "퇴실 마감 시간을 확인하는 곳", checkOutCoords, location);
        spots.add(spotRepository.save(checkOutSpot));

        log.info("스팟 5개 생성 완료 (SSAFY + 4개 추가 스팟)");
        return spots;
    }

    private List<Card> createCards(Location location, Spot ssafySpot) {
        List<Card> cards = new ArrayList<>();

        // 스팟 관련 카드 1개 생성
        Card spotCard = Card.from(
                location,
                ssafySpot,
                "SSAFY 특급카드",
                "SSAFY 스팟에서 얻을 수 있는 특별한 카드입니다",
                90, // baseAttack
                80, // baseDefense
                true // isSpotCard
        );
        Card savedSpotCard = cardRepository.save(spotCard);
        cards.add(savedSpotCard);

        // 일반 카드 4개 생성
        String[] cardNames = {"하늘빛천사 컨설턴트님", "번개질주 실습코치님", "태양의수호자 실습코치님", "어둠의추격자 프로님"};
        String[] cardDescriptions = {
                "컨설턴트님",
                "실습 코치님",
                "실습 코치님",
                "프로님"
        };
        int[] attacks = {100, 75, 85, 95};
        int[] defenses = {100, 50, 65, 90};

        for (int i = 0; i < 4; i++) {
            Card card = Card.from(
                    location,
                    null, // 스팟과 연결되지 않음
                    cardNames[i],
                    cardDescriptions[i],
                    attacks[i],
                    defenses[i],
                    false // isSpotCard
            );
            Card saved = cardRepository.save(card);
            cards.add(saved);
        }

        log.info("카드 5개 생성 완료 (스팟 관련 1개, 일반 카드 4개)");
        return cards;
    }

    private List<QuizProblem> createAllQuizProblems(List<Spot> spots) {
        List<QuizProblem> problems = new ArrayList<>();

        // 1. SSAFY 스팟 퀴즈 (기존)
        List<String> ssafyChoices = List.of(
                "1. 삼성 청년 SW 아카데미",
                "2. 삼성 청년 소프트웨어 AI 아카데미",
                "3. 삼성 소프트웨어 아카데미",
                "4. 삼성 청년 개발자 아카데미"
        );
        QuizProblem ssafyProblem = QuizProblem.from(
                spots.get(0), // SSAFY 스팟
                3,
                "SSAFY는 무엇의 줄임말인가요?",
                ssafyChoices,
                1, // 정답: 2번
                "SSAFY는 Samsung Software AI Academy For Youth의 줄임말로, 삼성 청년 소프트웨어 아카데미입니다."
        );
        problems.add(quizProblemRepository.save(ssafyProblem));

        // 2. 학생 수 퀴즈
        List<String> studentCountChoices = List.of(
                "1. 40명",
                "2. 50명",
                "3. 53명",
                "4. 54명"
        );
        QuizProblem studentCountProblem = QuizProblem.from(
                spots.get(1), // 학생 수 스팟
                3,
                "우리반 학생 수는?",
                studentCountChoices,
                3, // 정답: 4번 (54명)
                "우리 반은 총 54명의 학생으로 구성되어 있습니다."
        );
        problems.add(quizProblemRepository.save(studentCountProblem));

        // 3. 도서관 위치 퀴즈
        List<String> libraryChoices = List.of(
                "1. 맨 뒤",
                "2. 맨 앞",
                "3. 문 쪽",
                "4. 창문 쪽"
        );
        QuizProblem libraryProblem = QuizProblem.from(
                spots.get(2), // 도서관 위치 스팟
                3,
                "우리 반의 도서관 위치는?",
                libraryChoices,
                0, // 정답: 1번 (맨 뒤)
                "우리 반의 도서관은 교실 맨 뒤쪽에 위치하고 있습니다."
        );
        problems.add(quizProblemRepository.save(libraryProblem));

        // 4. 입실 시간 퀴즈
        List<String> checkInChoices = List.of(
                "1. 08:50",
                "2. 09:00",
                "3. 10:00",
                "4. 09:59"
        );
        QuizProblem checkInProblem = QuizProblem.from(
                spots.get(3), // 입실 시간 스팟
                3,
                "입실 마감 시간은?",
                checkInChoices,
                1, // 정답: 2번 (09:00)
                "입실 마감 시간은 오전 9시입니다."
        );
        problems.add(quizProblemRepository.save(checkInProblem));

        // 5. 퇴실 시간 퀴즈
        List<String> checkOutChoices = List.of(
                "1. 18:00",
                "2. 17:59",
                "3. 18:30",
                "4. 19:00"
        );
        QuizProblem checkOutProblem = QuizProblem.from(
                spots.get(4), // 퇴실 시간 스팟
                3,
                "퇴실 마감 시간은?",
                checkOutChoices,
                2, // 정답: 3번 (18:30)
                "퇴실 마감 시간은 오후 6시 30분입니다."
        );
        problems.add(quizProblemRepository.save(checkOutProblem));

        log.info("퀴즈 5개 생성 완료 (각 스팟별 1개씩)");
        return problems;
    }


    private Location createLocation() {
        // Location.create() 팩토리 메서드 사용
        Coordinates coordinates = Coordinates.create(
                new BigDecimal("37.5012743"),
                new BigDecimal("127.0396220")
        );
        Location location = Location.create("SSAFY", coordinates);

        Location saved = locationRepository.save(location);
        log.info("위치 생성: SSAFY");
        return saved;
    }

    private Teacher createTeacher() {
        // Teacher.from()만 사용 (User 상속받으므로 자동으로 users, teachers 둘 다 저장됨)
        Teacher teacher = Teacher.from("teacher1", passwordEncoder.encode("password1"), "김선생");
        Teacher savedTeacher = teacherRepository.save(teacher);

        log.info("선생님 계정 생성: teacher1 (users, teachers 테이블 모두 저장)");
        return savedTeacher;
    }

    private List<User> createStudents() {
        List<User> students = new ArrayList<>();
        String[] teamNames = {"ARGO", "그라데이션", "1중대", "랩핑랩핑", "E1l5",
                "떡잎유치원", "싸자보이즈", "큰일레슨", "Be효율", "싸피"};

        // 10개 팀 * 6명씩 = 60명
        for (int team = 1; team <= 10; team++) {
            for (int member = 1; member <= 6; member++) {
                String username = String.format("student%d%d", team, member);
                String name = String.format("%s_%d번", teamNames[team-1], member);

                // User.from() 사용
                User student = User.from(username, passwordEncoder.encode("password1"), name, Role.ROLE_STUDENT);
                User saved = userRepository.save(student);
                students.add(saved);
            }
        }
        log.info("학생 60명 생성 완료");
        return students;
    }

    private ClassRoom createClassRoom(Teacher teacher, Location location) {
        // ClassRoom.from() 사용
        ClassRoom classRoom = ClassRoom.from(
                teacher,
                "싸피3반",
                "SSAFY 3기 실습반입니다",
                LocalDate.of(2025, 10, 1),
                "SSAFY3",
                60,
                location,
                3
        );
        ClassRoom saved = classRoomRepository.save(classRoom);
        log.info("반 생성: 싸피3반");
        return saved;
    }

    private void applyStudentsToClass(List<User> students, ClassRoom classRoom) {
        for (User student : students) {
            // ClassApplication.from() 사용
            ClassApplication application = ClassApplication.from(student, classRoom);
            // 상태를 APPROVED로 직접 설정
            application.setStatus(ApplicationStatus.APPROVED);
            classApplicationRepository.save(application);
        }
        log.info("학생 60명 반 신청 및 승인 완료");
    }

    private List<Team> createTeams(ClassRoom classRoom) {
        String[] teamNames = {"ARGO", "그라데이션", "1중대", "랩핑랩핑", "E1l5",
                "떡잎유치원", "싸자보이즈", "큰일레슨", "Be효율", "싸피"};

        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // Team.from() 사용 (리더는 나중에 설정)
            Team team = Team.from(classRoom, teamNames[i], 6);
            Team saved = teamRepository.save(team);
            teams.add(saved);
        }
        log.info("팀 10개 생성 완료");
        return teams;
    }

    private void assignStudentsToTeams(List<User> students, List<Team> teams) {
        for (int team = 0; team < 10; team++) {
            Team currentTeam = teams.get(team);
            User leader = null;

            // 각 팀에 6명 배정
            for (int member = 0; member < 6; member++) {
                int studentIndex = team * 6 + member;
                User student = students.get(studentIndex);

                // UserTeam.create() 사용
                UserTeam userTeam = UserTeam.create(student, currentTeam);
                userTeamRepository.save(userTeam);

                // 첫 번째 멤버(번호가 1인 학생)를 팀장으로 설정
                if (member == 0) {
                    leader = student;
                }
            }

            // 팀장 설정
            currentTeam.setLeader(leader);
            teamRepository.save(currentTeam);
        }
        log.info("학생들 팀 배정 및 팀장 설정 완료");
    }
}