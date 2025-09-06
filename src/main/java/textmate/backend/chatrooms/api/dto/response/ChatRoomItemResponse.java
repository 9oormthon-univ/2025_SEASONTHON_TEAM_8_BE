package textmate.backend.chatrooms.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

@Getter
@Builder
public class ChatRoomItemResponse {
    private String id;             // uuid 문자열
    private String name;
    private ChatRoomType type;     // GROUP | PRIVATE
    private boolean pinned;        // 내가 고정했는지
    private boolean deleted;       // 소프트 삭제 여부
    private String lastMessageAt;  // ISO-8601 문자열 (nullable)
    private String createdAt;      // ISO-8601 문자열

    private String avatarText;
}