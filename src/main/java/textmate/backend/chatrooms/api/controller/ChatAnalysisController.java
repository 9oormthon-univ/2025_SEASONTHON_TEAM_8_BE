package textmate.backend.chatrooms.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatrooms.api.dto.request.ChatAnalysisRequest;
import textmate.backend.chatrooms.api.dto.request.UpdateChatRoomRequest;
import textmate.backend.chatrooms.api.dto.response.ChatAnalysisResponse;
import textmate.backend.chatrooms.api.dto.response.ChatRoomResponse;
import textmate.backend.chatrooms.application.ChatAnalysisService;
import textmate.backend.chatrooms.application.ChatRoomCommandService;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

@Slf4j
@RestController
@RequestMapping("/api/chat/analysis")
@RequiredArgsConstructor
public class ChatAnalysisController {

    private final ChatAnalysisService chatAnalysisService;
    private final ChatRoomCommandService service;

    //카톡 대화 분석 요청 (단체톡방/개인톡방 선택)
    //방생성
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChatAnalysisResponse> analyzeChat(
            @RequestParam("chatRoomType") ChatRoomType chatRoomType,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            log.info("채팅 분석 요청 받음 - 타입: {}, 파일명: {}", chatRoomType, file.getOriginalFilename());
            
            // 입력 검증
            if (file == null || file.isEmpty()) {
                log.warn("파일이 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            if (chatRoomType == null) {
                log.warn("채팅방 타입이 null");
                return ResponseEntity.badRequest().build();
            }

            ChatAnalysisRequest request = new ChatAnalysisRequest();
            request.setFile(file);
            request.setChatRoomType(chatRoomType);

            ChatAnalysisResponse response = chatAnalysisService.analyze(request);
            log.info("채팅 분석 완료 - ID: {}", response.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("채팅 분석 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /chatrooms/{roomId}
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomResponse> getOne(@PathVariable String roomId) {
        try {
            log.info("채팅방 조회 요청 - roomId: {}", roomId);
            // 로그인 필요 없으니 userId 제거
            ChatRoomResponse response = service.getOne(null, roomId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("채팅방 조회 중 오류 발생 - roomId: {}", roomId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** 1.4 PATCH /chatrooms/{roomId} */
    @PatchMapping("/{roomId}")
    public ResponseEntity<ChatRoomResponse> patch(
            @PathVariable String roomId,
            @RequestBody UpdateChatRoomRequest request
    ) {
        try {
            log.info("채팅방 수정 요청 - roomId: {}", roomId);
            ChatRoomResponse response = service.patch(null, roomId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("채팅방 수정 중 오류 발생 - roomId: {}", roomId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** 1.5 DELETE /chatrooms/{roomId} */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(@PathVariable String roomId) {
        try {
            log.info("채팅방 삭제 요청 - roomId: {}", roomId);
            service.softDelete(roomId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("채팅방 삭제 중 오류 발생 - roomId: {}", roomId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}