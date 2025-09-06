package textmate.backend.auth_google.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import textmate.backend.auth_google.api.dto.response.GoogleTokenDto;
import textmate.backend.auth_google.application.UserGoogleLoginService;

@Slf4j
@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping("login/oauth2/code/google")

public class GoogleController{
    private final UserGoogleLoginService userGoogleLoginService;

    @GetMapping
    public ResponseEntity<GoogleTokenDto> googleLogin(@RequestParam(name = "code") String code) {
        GoogleTokenDto googleTokenDto = userGoogleLoginService.loginOrSignUp(code);
        System.out.println("구글 로그인 성공");
        return ResponseEntity.ok(googleTokenDto);
    }
}