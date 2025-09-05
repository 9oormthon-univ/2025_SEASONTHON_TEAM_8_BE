package textmate.backend.auth_google.api.dto.response;

import lombok.Getter;

@Getter
public class UserResponseDto {
    private Long id;
    private String email;
    private String password;
}