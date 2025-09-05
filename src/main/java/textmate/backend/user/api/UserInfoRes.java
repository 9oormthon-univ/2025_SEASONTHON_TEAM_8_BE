package textmate.backend.user.api;

import lombok.Builder;

@Builder
public record UserInfoRes(
        Long userId,
        String name,
        String email
) {
}