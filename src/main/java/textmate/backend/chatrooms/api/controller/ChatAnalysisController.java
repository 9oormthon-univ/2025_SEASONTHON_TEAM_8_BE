package textmate.backend.chatrooms.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatrooms.api.dto.request.ChatAnalysisRequest;
import textmate.backend.chatrooms.api.dto.request.UpdateChatRoomRequest;
import textmate.backend.chatrooms.api.dto.response.ChatAnalysisResponse;
import textmate.backend.chatrooms.api.dto.response.ChatRoomResponse;
import textmate.backend.chatrooms.application.ChatAnalysisService;
import textmate.backend.chatrooms.application.ChatRoomCommandService;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

@RestController
@RequestMapping("/api/chat/analysis")
@RequiredArgsConstructor
public class ChatAnalysisController {

    private final ChatAnalysisService chatAnalysisService;
    private final ChatRoomCommandService service;

    //카톡 대화 분석 요청 (단체톡방/개인톡방 선택)
    //방생성
    @PostMapping
    public ResponseEntity<ChatAnalysisResponse> analyzeChat(
            @RequestParam("chatRoomType") ChatRoomType chatRoomType,
            @RequestParam("file") MultipartFile file
    ) {
        ChatAnalysisRequest request = new ChatAnalysisRequest();
        request.setFile(file);
        request.setChatRoomType(chatRoomType);

        ChatAnalysisResponse response = chatAnalysisService.analyze(request);
        return ResponseEntity.ok(response);
    }
    /** 1.3 GET /chatrooms/{roomId} */
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomResponse> getOne(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable String roomId
    ) {
        return ResponseEntity.ok(service.getOne(userId, roomId));
    }

    /** 1.4 PATCH /chatrooms/{roomId} */
    @PatchMapping("/{roomId}")
    public ResponseEntity<ChatRoomResponse> patch(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable String roomId,
            @RequestBody UpdateChatRoomRequest request
    ) {
        return ResponseEntity.ok(service.patch(userId, roomId, request));
    }

    /** 1.5 DELETE /chatrooms/{roomId}  → 204 No Content */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(@PathVariable String roomId) {
        service.softDelete(roomId);
        return ResponseEntity.noContent().build();
    }
}