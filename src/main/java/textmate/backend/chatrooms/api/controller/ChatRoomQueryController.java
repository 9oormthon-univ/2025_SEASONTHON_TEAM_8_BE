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
@Slf4j
@RestController
@RequestMapping("/api/chat/chatrooms")
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
        try {
            log.info("채팅방 목록 조회 요청 - userId: {}, query: {}, type: {}, sort: {}, page: {}, size: {}", 
                    userId, query, type, sort, page, size);
            
            // 입력 검증
            if (userId == null) {
                log.warn("사용자 ID가 null입니다");
                return ResponseEntity.badRequest().build();
            }
            
            if (page < 0) {
                log.warn("페이지 번호가 음수입니다: {}", page);
                return ResponseEntity.badRequest().build();
            }
            
            if (size <= 0 || size > 100) {
                log.warn("페이지 크기가 유효하지 않습니다: {}", size);
                return ResponseEntity.badRequest().build();
            }
            
            ChatRoomPageResponse response = chatRoomQueryService.getRooms(userId, query, type, sort, page, size);
            log.info("채팅방 목록 조회 완료 - 총 {}개", response.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("채팅방 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 전체 조회
    @GetMapping("/all")
    public ResponseEntity<List<ChatRoomItemResponse>> getAll(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "type", required = false) ChatRoomType type,
            @RequestParam(value = "sort", required = false, defaultValue = "DEFAULT") ChatRoomSort sort
    ) {
        try {
            log.info("채팅방 전체 조회 요청 - userId: {}, query: {}, type: {}, sort: {}", 
                    userId, query, type, sort);
            
            // 입력 검증
            if (userId == null) {
                log.warn("사용자 ID가 null입니다");
                return ResponseEntity.badRequest().build();
            }
            
            List<ChatRoomItemResponse> response = chatRoomQueryService.getAllRooms(userId, query, type, sort);
            log.info("채팅방 전체 조회 완료 - 총 {}개", response.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("채팅방 전체 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}