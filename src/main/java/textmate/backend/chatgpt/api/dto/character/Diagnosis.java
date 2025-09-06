package textmate.backend.chatgpt.api.dto.character;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Diagnosis {
    private String relationInsight;  // "지금 당신과의 관계는?"
    private String actions;          // "이 관계를 해결하기 위한 조언"
    private String context;          // "대화 맥락 파악(친구/직장/가족 등)"
}