package textmate.backend.chat.api.dto;

import lombok.Builder;
import lombok.Getter;
import textmate.backend.chat.domain.ChatAnalysisHistory;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatAnalysisHistoryRes {
    private Long id;
    private String relationshipType;
    private int messageCount;
    private String summary;
    private LocalDateTime createdAt;

    public static ChatAnalysisHistoryRes from(ChatAnalysisHistory h) {
        return ChatAnalysisHistoryRes.builder()
                .id(h.getId())
                .relationshipType(h.getRelationshipType().name())
                .messageCount(h.getMessageCount())
                .summary(h.getSummary())
                .createdAt(h.getCreatedAt())
                .build();
    }
}