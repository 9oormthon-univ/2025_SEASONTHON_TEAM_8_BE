package textmate.backend.chatrooms.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

@Getter
@Builder
public class ChatRoomResponse {
    private String id;
    private String name;
    private ChatRoomType type;
    private boolean pinned;     // 사용자 기준 고정 여부
    private boolean deleted;    // 소프트 삭제 플래그
    private String createdAt;
    private String updatedAt;   // null 가능
}