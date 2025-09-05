package textmate.backend.chat.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatAnalysisResponse {
    private Long historyId;
    private String relationshipType;
    private int messageCount;
    private String summary;
}