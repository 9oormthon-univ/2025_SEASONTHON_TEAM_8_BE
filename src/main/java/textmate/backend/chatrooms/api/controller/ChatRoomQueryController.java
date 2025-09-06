package textmate.backend.chatrooms.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import textmate.backend.chatrooms.api.dto.response.ChatRoomItemResponse;
import textmate.backend.chatrooms.api.dto.response.ChatRoomPageResponse;
import textmate.backend.chatrooms.application.ChatRoomQueryService;
import textmate.backend.chatrooms.domain.Enum.ChatRoomSort;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

import java.util.List;

/**
 * 방 목록 조회 컨트롤러 (DB 미사용)
 * GET /chatrooms?query=&type=&sort=&page=0&size=20
 */

/**
 * 방 목록 조회 컨트롤러 (DB 미사용)
 * GET /chatrooms?query=&type=&sort=&page=0&size=20
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/chatrooms")
@RequiredArgsConstructor
public class ChatRoomQueryController {

    private final ChatRoomQueryService chatRoomQueryService;

    @GetMapping("/all")
    public ResponseEntity<List<ChatRoomItemResponse>> getAll(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "type", required = false) ChatRoomType type,
            @RequestParam(value = "sort", required = false, defaultValue = "DEFAULT") ChatRoomSort sort
    ) {
        try {
            Long userId = 1L; // ✅ 로컬 테스트용 하드코딩

            log.info("채팅방 전체 조회 요청 - userId: {}, query: {}, type: {}, sort: {}",
                    userId, query, type, sort);

            List<ChatRoomItemResponse> response = chatRoomQueryService.getAllRooms(userId, query, type, sort);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("채팅방 전체 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}