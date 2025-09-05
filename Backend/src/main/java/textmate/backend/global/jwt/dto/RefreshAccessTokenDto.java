package textmate.backend.global.jwt.dto;

import lombok.Builder;

@Builder
public record RefreshAccessTokenDto(
        String refreshAccessToken
) {
}