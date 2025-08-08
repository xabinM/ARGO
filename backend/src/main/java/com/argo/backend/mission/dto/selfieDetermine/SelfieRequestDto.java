package com.argo.backend.mission.dto.selfieDetermine;

import com.argo.backend.domain.ploblem.enums.PhotoPose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class SelfieRequestDto {

    private MultipartFile multipartFile;
    private PhotoPose pose;
}
