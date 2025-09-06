package textmate.backend.chatgpt.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import textmate.backend.chatgpt.api.organization.RelationshipAnalysisResponse;
import textmate.backend.chatgpt.application.RelationshipAnalysisService;
import textmate.backend.chatrooms.domain.Enum.ChatRoomType;

@Slf4j
@RestController
@RequestMapping("/api/chat-analysis/relationship")
@RequiredArgsConstructor
public class RelationshipAnalysisController {

    private final RelationshipAnalysisService service;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RelationshipAnalysisResponse> analyze(
            @RequestParam("type") ChatRoomType type,          // GROUP | PRIVATE
            @RequestPart("file") MultipartFile file           // 카톡 txt
    ) {
        try {
            log.info("관계 분석 요청 받음 - 타입: {}, 파일명: {}", type, file.getOriginalFilename());

            // 입력 검증
            if (file == null || file.isEmpty()) {
                log.warn("파일이 비어있음");
                return ResponseEntity.badRequest().build();
            }

            if (type == null) {
                log.warn("채팅방 타입이 null");
                return ResponseEntity.badRequest().build();
            }

            RelationshipAnalysisResponse response = service.analyze(type, file);
            log.info("관계 분석 완료");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("관계 분석 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}