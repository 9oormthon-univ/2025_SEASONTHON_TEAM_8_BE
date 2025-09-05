package textmate.backend.auth_google.api.dto.request;

import lombok.Builder;
import lombok.Getter;
@Getter
@Builder
public class UserRequestDto {
    private String name; //회원가입시 name
    private String email;
    private String password;
}