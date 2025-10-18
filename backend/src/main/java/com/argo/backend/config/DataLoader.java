package com.argo.backend.config;

import com.argo.backend.domain.cardgame.entity.Card;
import com.argo.backend.domain.cardgame.repository.CardRepository;
import com.argo.backend.domain.classroom.entity.ClassApplication;
import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.classroom.enums.ApplicationStatus;
import com.argo.backend.domain.classroom.repository.ClassApplicationRepository;
import com.argo.backend.domain.classroom.repository.ClassRoomRepository;
import com.argo.backend.domain.common.Coordinates;
import com.argo.backend.domain.location.entity.Location;
import com.argo.backend.domain.location.repository.LocationRepository;
import com.argo.backend.domain.ploblem.entity.QuizProblem;
import com.argo.backend.domain.ploblem.repository.QuizProblemRepository;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.spot.repository.SpotRepository;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.user.entity.Teacher;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.entity.UserTeam;
import com.argo.backend.domain.user.enums.Role;
import com.argo.backend.domain.user.repository.TeacherRepository;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.domain.user.repository.UserTeamRepository;
import lombok.RequiredArgsConstructor;
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
    private final QuizProblemRepository quizProblemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.count() > 10) {
            return;
        }

        Location ssafyLocation = createLocation();
        Teacher teacher = createTeacher();
        List<User> students = createStudents();
        ClassRoom classRoom = createClassRoom(teacher, ssafyLocation);
        applyStudentsToClass(students, classRoom);
        List<Team> teams = createTeams(classRoom);
        assignStudentsToTeams(students, teams);
        List<Spot> spots = createAllSpots(ssafyLocation);
        createCards(ssafyLocation, spots.get(0));
        createAllQuizProblems(spots);
    }

    private List<Spot> createAllSpots(Location location) {
        List<Spot> spots = new ArrayList<>();

        Coordinates ssafyCoords = Coordinates.create(
                new BigDecimal("37.5012743"),
                new BigDecimal("127.0396220")
        );
        Spot ssafySpot = Spot.create("SSAFY 스팟", "SSAFY 교육장 메인 스팟", ssafyCoords, location);
        spots.add(spotRepository.save(ssafySpot));

        Coordinates studentCountCoords = Coordinates.create(
                new BigDecimal("37.501347"),
                new BigDecimal("127.039765")
        );
        Spot studentCountSpot = Spot.create("학생 수 체크 스팟", "우리 반 학생 수를 확인하는 곳", studentCountCoords, location);
        spots.add(spotRepository.save(studentCountSpot));

        Coordinates libraryCoords = Coordinates.create(
                new BigDecimal("37.501280"),
                new BigDecimal("127.039790")
        );
        Spot librarySpot = Spot.create("도서관 위치 스팟", "우리 반 도서관 위치를 확인하는 곳", libraryCoords, location);
        spots.add(spotRepository.save(librarySpot));

        Coordinates checkInCoords = Coordinates.create(
                new BigDecimal("37.501227"),
                new BigDecimal("127.039658")
        );
        Spot checkInSpot = Spot.create("입실 시간 스팟", "입실 마감 시간을 확인하는 곳", checkInCoords, location);
        spots.add(spotRepository.save(checkInSpot));

        Coordinates checkOutCoords = Coordinates.create(
                new BigDecimal("37.501249"),
                new BigDecimal("127.039600")
        );
        Spot checkOutSpot = Spot.create("퇴실 시간 스팟", "퇴실 마감 시간을 확인하는 곳", checkOutCoords, location);
        spots.add(spotRepository.save(checkOutSpot));

        return spots;
    }

    private List<Card> createCards(Location location, Spot ssafySpot) {
        List<Card> cards = new ArrayList<>();

        Card spotCard = Card.from(
                location,
                ssafySpot,
                "SSAFY 특급카드",
                "SSAFY 스팟에서 얻을 수 있는 특별한 카드입니다",
                90, 
                80, 
                true
        );
        Card savedSpotCard = cardRepository.save(spotCard);
        cards.add(savedSpotCard);

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
                    null,
                    cardNames[i],
                    cardDescriptions[i],
                    attacks[i],
                    defenses[i],
                    false
            );
            Card saved = cardRepository.save(card);
            cards.add(saved);
        }

        return cards;
    }

    private List<QuizProblem> createAllQuizProblems(List<Spot> spots) {
        List<QuizProblem> problems = new ArrayList<>();

        List<String> ssafyChoices = List.of(
                "1. 삼성 청년 SW 아카데미",
                "2. 삼성 청년 소프트웨어 AI 아카데미",
                "3. 삼성 소프트웨어 아카데미",
                "4. 삼성 청년 개발자 아카데미"
        );
        QuizProblem ssafyProblem = QuizProblem.from(
                spots.get(0),
                3,
                "SSAFY는 무엇의 줄임말인가요?",
                ssafyChoices,
                1,
                "SSAFY는 Samsung Software AI Academy For Youth의 줄임말로, 삼성 청년 소프트웨어 아카데미입니다."
        );
        problems.add(quizProblemRepository.save(ssafyProblem));

        List<String> studentCountChoices = List.of(
                "1. 40명",
                "2. 50명",
                "3. 53명",
                "4. 54명"
        );
        QuizProblem studentCountProblem = QuizProblem.from(
                spots.get(1),
                3,
                "우리반 학생 수는?",
                studentCountChoices,
                3,
                "우리 반은 총 54명의 학생으로 구성되어 있습니다."
        );
        problems.add(quizProblemRepository.save(studentCountProblem));

        List<String> libraryChoices = List.of(
                "1. 맨 뒤",
                "2. 맨 앞",
                "3. 문 쪽",
                "4. 창문 쪽"
        );
        QuizProblem libraryProblem = QuizProblem.from(
                spots.get(2),
                3,
                "우리 반의 도서관 위치는?",
                libraryChoices,
                0,
                "우리 반의 도서관은 교실 맨 뒤쪽에 위치하고 있습니다."
        );
        problems.add(quizProblemRepository.save(libraryProblem));

        List<String> checkInChoices = List.of(
                "1. 08:50",
                "2. 09:00",
                "3. 10:00",
                "4. 09:59"
        );
        QuizProblem checkInProblem = QuizProblem.from(
                spots.get(3),
                3,
                "입실 마감 시간은?",
                checkInChoices,
                1,
                "입실 마감 시간은 오전 9시입니다."
        );
        problems.add(quizProblemRepository.save(checkInProblem));

        List<String> checkOutChoices = List.of(
                "1. 18:00",
                "2. 17:59",
                "3. 18:30",
                "4. 19:00"
        );
        QuizProblem checkOutProblem = QuizProblem.from(
                spots.get(4),
                3,
                "퇴실 마감 시간은?",
                checkOutChoices,
                2,
                "퇴실 마감 시간은 오후 6시 30분입니다."
        );
        problems.add(quizProblemRepository.save(checkOutProblem));

        return problems;
    }


    private Location createLocation() {
        Coordinates coordinates = Coordinates.create(
                new BigDecimal("37.5012743"),
                new BigDecimal("127.0396220")
        );
        Location location = Location.create("SSAFY", coordinates);

        return locationRepository.save(location);
    }

    private Teacher createTeacher() {
        Teacher teacher = Teacher.from("teacher1", passwordEncoder.encode("password1"), "김선생");
        return teacherRepository.save(teacher);
    }

    private List<User> createStudents() {
        List<User> students = new ArrayList<>();
        String[] teamNames = {"ARGO", "그라데이션", "1중대", "랩핑랩핑", "E1l5",
                "떡잎유치원", "싸자보이즈", "큰일레슨", "Be효율", "싸피"};

        for (int team = 1; team <= 10; team++) {
            for (int member = 1; member <= 6; member++) {
                String username = String.format("student%d%d", team, member);
                String name = String.format("%s_%d번", teamNames[team-1], member);

                User student = User.from(username, passwordEncoder.encode("password1"), name, Role.ROLE_STUDENT);
                User saved = userRepository.save(student);
                students.add(saved);
            }
        }
        return students;
    }

    private ClassRoom createClassRoom(Teacher teacher, Location location) {
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
        return classRoomRepository.save(classRoom);
    }

    private void applyStudentsToClass(List<User> students, ClassRoom classRoom) {
        for (User student : students) {
            ClassApplication application = ClassApplication.from(student, classRoom);
            application.setStatus(ApplicationStatus.APPROVED);
            classApplicationRepository.save(application);
        }
    }

    private List<Team> createTeams(ClassRoom classRoom) {
        String[] teamNames = {"ARGO", "그라데이션", "1중대", "랩핑랩핑", "E1l5",
                "떡잎유치원", "싸자보이즈", "큰일레슨", "Be효율", "싸피"};

        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Team team = Team.from(classRoom, teamNames[i], 6);
            Team saved = teamRepository.save(team);
            teams.add(saved);
        }
        return teams;
    }

    private void assignStudentsToTeams(List<User> students, List<Team> teams) {
        for (int team = 0; team < 10; team++) {
            Team currentTeam = teams.get(team);
            User leader = null;

            for (int member = 0; member < 6; member++) {
                int studentIndex = team * 6 + member;
                User student = students.get(studentIndex);

                UserTeam userTeam = UserTeam.create(student, currentTeam);
                userTeamRepository.save(userTeam);

                if (member == 0) {
                    leader = student;
                }
            }

            currentTeam.setLeader(leader);
            teamRepository.save(currentTeam);
        }
    }
}