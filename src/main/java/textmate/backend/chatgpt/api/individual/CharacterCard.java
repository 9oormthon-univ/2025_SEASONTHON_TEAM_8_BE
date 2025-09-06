package textmate.backend.chatgpt.api.individual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public @Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CharacterCard {
    private String name;          // 이름(없으면 "나"/"상대방")
    private String role;          // 캐릭터/역할 별칭 (예: "공감형 리스너")
    private String description;   // 한 줄 설명
    private List<String> strengths;   // 강점
    private List<String> weaknesses;  // 보완점
    private String advice;            // 개인 조언
}
