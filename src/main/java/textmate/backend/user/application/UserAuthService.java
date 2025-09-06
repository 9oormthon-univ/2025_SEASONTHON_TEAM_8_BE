package textmate.backend.user.application;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import textmate.backend.global.jwt.TokenProvider;
import textmate.backend.user.api.UserInfoRes;
import textmate.backend.user.domain.User;
import textmate.backend.user.domain.repository.UserRefreshTokenRepository;
import textmate.backend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthService {
    private final UserRepository userRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;

    private final TokenProvider tokenProvider;
    private final TokenRefreshService tokenRefreshService;

    public UserInfoRes getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다."));

        return UserInfoRes.builder()
                .userId(userId)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다."));

        userRefreshTokenRepository.deleteByUserImmediate(user);
    }
}