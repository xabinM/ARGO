package com.argo.backend.auth.dto.reissue;

import com.argo.backend.auth.dto.common.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReissueResponse {
    private TokenDto tokens;
}
