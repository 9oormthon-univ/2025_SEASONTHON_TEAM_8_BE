package textmate.backend.chatrooms.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

@Getter
@Setter
@NoArgsConstructor
public class UpdateChatRoomRequest {
    private String name;               // optional
    private ChatRoomType type;         // optional (GROUP | PRIVATE)
}