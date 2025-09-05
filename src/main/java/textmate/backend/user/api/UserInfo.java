package textmate.backend.user.api;

import lombok.Builder;

@Builder
public record UserInfo(
        String email,
        String name,
        String profileImageUrl
) {}