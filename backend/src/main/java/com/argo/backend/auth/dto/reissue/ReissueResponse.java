package com.argo.backend.auth.dto.reissue;

import com.argo.backend.auth.dto.common.Tokens;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReissueResponse {
    private Tokens tokens;
}
