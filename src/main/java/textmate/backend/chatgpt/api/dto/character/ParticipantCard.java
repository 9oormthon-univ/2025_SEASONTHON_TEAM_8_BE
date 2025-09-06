package textmate.backend.chatgpt.api.dto.character;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipantCard {
    private String name;           // 닉네임/이름
    private int messageCount;      // 메시지 수
    private double participation;  // 참여율(0~1)
    private String persona;        // 캐릭터/성향 설명
    private String sentiment;      // 감정 경향(긍/부/중립)
}