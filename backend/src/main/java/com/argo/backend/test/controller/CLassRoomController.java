package com.argo.backend.test.controller;

import com.argo.backend.test.dto.ClassCreateRequest;
import com.argo.backend.test.dto.ClassCreateResponse;
import com.argo.backend.test.dto.CommonApiResponse;
import com.argo.backend.test.service.ClassRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classes")
public class CLassRoomController {

    private final ClassRoomService classRoomService;

    @PostMapping("/create")
    public ResponseEntity<CommonApiResponse<ClassCreateResponse>> createClass(
            @RequestBody ClassCreateRequest request,
            @RequestHeader("User-Id") Long userId
    ) {
        // User-Id 헤더 검증
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
        }

        // 반 생성 서비스 호출
        ClassCreateResponse response = classRoomService.createClass(userId, request);

        log.info("반 생성 성공 - classId: {}, teacherId: {}", response.classId(), userId);

        return ResponseEntity.ok()
                .body(new CommonApiResponse<>(true, "반이 생성되었습니다.", response));
    }

}
