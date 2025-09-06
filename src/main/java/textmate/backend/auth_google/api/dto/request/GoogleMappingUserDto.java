package textmate.backend.auth_google.api.dto.request;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
//1. 회원가입 -> 구글 정보 가져오기.
//2. 로그인 -> 구글이 유저 정보 JSON 형태로 내려준 것을 매핑해놓는 DTO
public class GoogleMappingUserDto {
    private String id;
    private String name; //1번 회원가입시 name
    private String email;
    @SerializedName("profile_image")
    private String profileImage; //구글 프로필 가져오기
}