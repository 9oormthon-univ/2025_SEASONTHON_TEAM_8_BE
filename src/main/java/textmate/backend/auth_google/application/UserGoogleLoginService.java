package textmate.backend.auth_google.application;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import textmate.backend.auth_google.api.dto.request.GoogleMappingUserDto;
import textmate.backend.auth_google.api.dto.response.GoogleTokenDto;
import textmate.backend.global.jwt.TokenProvider;
import textmate.backend.user.domain.User;
import textmate.backend.user.domain.repository.UserRepository;

@Service
public class UserGoogleLoginService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final TokenProvider tokenProvider;

    private final String GOOGLE_CLIENT_ID;
    private final String GOOGLE_CLIENT_SECRET;
    private final String GOOGLE_REDIRECT_URI; // 환경변수 나중에 배포 주소로 변경해야함

    private static final String GOOGLE_USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    public UserGoogleLoginService(UserRepository userRepository, TokenProvider tokenProvider,
                                  @Value("${spring.google.client-id}") String clientId,
                                  @Value("${spring.google.client-secret}") String clientSecret,
                                  @Value("${spring.google.redirect-uri}") String redirectUri) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.GOOGLE_CLIENT_ID = clientId;
        this.GOOGLE_CLIENT_SECRET = clientSecret;
        this.GOOGLE_REDIRECT_URI = redirectUri;
    }

    @Transactional
    public GoogleTokenDto loginOrSignUp(String code) {
        String googleAccesstoken = exchangeAuthCodeForAccessToken(code);
        GoogleMappingUserDto googleUser = getGoogleUserInfo(googleAccesstoken);

        User user = userRepository.findByEmail(googleUser.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(googleUser.getEmail())
                            .name(googleUser.getName())
                            .profileImage(googleUser.getProfileImage())
                            .build();
                    return userRepository.save(newUser);
                });

        String accessToken = tokenProvider.createAccessToken(user);
        String refreshToken = tokenProvider.createRefreshToken(user);

        return GoogleTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private GoogleMappingUserDto getGoogleUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GOOGLE_USERINFO_URL,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            String json = response.getBody();
            Gson gson = new Gson();
            return gson.fromJson(json, GoogleMappingUserDto.class);
        }

        throw new RuntimeException("구글 사용자 정보를 가져오는 데 실패했습니다.");
    }

    public String exchangeAuthCodeForAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", GOOGLE_CLIENT_ID);
        params.add("client_secret", GOOGLE_CLIENT_SECRET);
        params.add("redirect_uri", GOOGLE_REDIRECT_URI); // 환경변수 나중에 배포 주소로 변경해야함

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                request,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            // 3) JSON에서 access_token 추출
            //    (이 예시에선 Gson이나 Jackson 등을 사용)
            Gson gson = new Gson();
            JsonObject jsonObj = gson.fromJson(response.getBody(), JsonObject.class);
            return jsonObj.get("access_token").getAsString();
        } else {
            throw new RuntimeException("구글 OAuth 토큰 교환에 실패했습니다. " + response.getStatusCode());
        }
    }
}