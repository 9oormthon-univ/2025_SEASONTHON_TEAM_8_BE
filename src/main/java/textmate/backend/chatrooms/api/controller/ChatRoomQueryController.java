package textmate.backend.chatrooms.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import textmate.backend.chatrooms.api.dto.response.ChatRoomPageResponse;
import textmate.backend.chatrooms.application.ChatRoomQueryService;
import textmate.backend.chatrooms.domain.Enum.ChatRoomSort;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

/**
 * 방 목록 조회 컨트롤러 (DB 미사용)
 * GET /chatrooms?query=&type=&sort=&page=0&size=20
 */
@RestController
@RequestMapping("/api/chat//chatrooms")
@RequiredArgsConstructor
public class ChatRoomQueryController {

    private final ChatRoomQueryService chatRoomQueryService;


    @GetMapping
    public ResponseEntity<ChatRoomPageResponse> getRooms(
            @AuthenticationPrincipal(expression = "id") Long userId,  // Google UserPrincipal.id 가정
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "type", required = false) ChatRoomType type,
            @RequestParam(value = "sort", required = false, defaultValue = "DEFAULT") ChatRoomSort sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                chatRoomQueryService.getRooms(userId, query, type, sort, page, size)
        );
    }
}