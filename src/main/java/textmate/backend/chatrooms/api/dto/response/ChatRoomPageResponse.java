package textmate.backend.chatrooms.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatRoomPageResponse {
    private List<ChatRoomItemResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
