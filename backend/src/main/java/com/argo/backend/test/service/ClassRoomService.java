package com.argo.backend.test.service;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.classroom.ClassStatus;
import com.argo.backend.domain.location.ClassLocation;
import com.argo.backend.domain.location.Location;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.test.dto.ClassCreateRequest;
import com.argo.backend.test.dto.ClassCreateResponse;
import com.argo.backend.test.repository.ClassLocationRepository;
import com.argo.backend.test.repository.ClassRoomRepository;
import com.argo.backend.test.repository.LocationRepository;
import com.argo.backend.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class ClassRoomService {

    private final ClassRoomRepository classRoomRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ClassLocationRepository classLocationRepository;

    private static final String INVITE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;

    @Transactional
    public ClassCreateResponse createClass(Long teacherId, ClassCreateRequest request) {

        // 사용자 존재 및 권한 확인
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 선생님 권한 확인
        if (!Role.TEACHER.equals(teacher.getRole())) {
            throw new SecurityException("선생님만 반을 생성할 수 있습니다.");
        }

        // 고유한 초대 코드 생성
        // 나중에 이건 코드로 대체할 것
        String inviteCode = generateUniqueInviteCode();

        // ClassRoom 엔티티 생성
        ClassRoom classRoom = ClassRoom.builder()
                .className(request.getClassName())
                .description(request.getDescription())
                .activityDate(request.getActivityDate())
                .maxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 30)
                .inviteCode(inviteCode)
                .teacher(teacher)
                .status(ClassStatus.ACTIVE)
                .build();

        // 저장
        ClassRoom saved = classRoomRepository.save(classRoom);

        // Location 처리 (데이터베이스에서 조회만, 없으면 오류)
        Location location = locationRepository.findByName(request.getLocation())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 활동 장소입니다. 올바른 장소명을 입력해주세요."));

        // ClassLocation 연결 생성
        ClassLocation.ClassLocationId id = new ClassLocation.ClassLocationId();
        id.setClassId(saved.getClassId());
        id.setLocationId(location.getLocationId());

        ClassLocation classLocation = ClassLocation.builder()
                .id(id)
                .classRoom(saved)
                .location(location)
                .build();
        classLocationRepository.save(classLocation);

        // 응답 DTO 생성 (record 사용)
        return new ClassCreateResponse(
                saved.getClassId(),
                saved.getClassName(),
                saved.getInviteCode(),
                saved.getTeacher().getUserId(),
                saved.getCreatedAt()
        );
    }

    /**
     * 중복되지 않는 고유한 초대 코드를 생성
     */
    private String generateUniqueInviteCode() {
        SecureRandom random = new SecureRandom();
        String code;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                int index = random.nextInt(INVITE_CHARACTERS.length());
                sb.append(INVITE_CHARACTERS.charAt(index));
            }
            code = sb.toString();
        } while (classRoomRepository.existsByInviteCode(code));

        return code;
    }
}
