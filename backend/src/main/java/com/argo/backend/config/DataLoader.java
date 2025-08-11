package com.argo.backend.config;

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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 이미 데이터가 있으면 생성하지 않음
        if (userRepository.count() > 50) {
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

        // 8. spot 추가

        // 9. card 추가

        log.info("=== 초기 테스트 데이터 생성 완료! ===");
        log.info("선생님: teacher1 / password1");
        log.info("학생들: student11~16, student21~26, ..., student101~106 / password1");
        log.info("반: 싸피3반 (SSAFY 위치)");
        log.info("팀: 10개 팀, 각 6명씩 배정");
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