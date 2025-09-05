package textmate.backend.user.api;

import lombok.Builder;

@Builder
public record UserLogInResDto(
        String accessToken,
        String refreshToken
) {
}
