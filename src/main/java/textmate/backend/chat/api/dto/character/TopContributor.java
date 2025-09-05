package textmate.backend.chat.api.dto.character;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopContributor {
    private String name;
    private int messageCount;
    private String brief;          // 요약 한 문장
}