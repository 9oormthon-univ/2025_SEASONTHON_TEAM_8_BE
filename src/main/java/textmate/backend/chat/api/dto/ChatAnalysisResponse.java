package textmate.backend.chat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChatAnalysisResponse {
    private Long historyId;
    private String relationshipType;
    private int messageCount;
    private String summary;
}